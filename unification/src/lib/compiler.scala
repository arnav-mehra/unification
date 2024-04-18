package lib.compiler
import lib.types.*
import lib.solver.*
import lib.unifier.*
import scala.collection.mutable.ArrayBuffer

class Compiler(
    val fn: Ast.Func,
    val vars: VarSet,
    val stack_ptr: VarId = 0
) {
    def compile(): RelSets = compile(fn)

    def compile(ast: Ast): RelSets = {
        ast match {
            case Ast.Func(ast, vars) => compile(ast)
            case Ast.Var(id) => RelSets()
            case Ast.BinOp(log_op: LogOp, left, right) => {
                var leftRels = compile(left)
                var rightRels = compile(right)
                log_op match {
                    case LogOp.And => leftRels.and(rightRels)
                    case LogOp.Or => leftRels.or(rightRels)
                }
            }
            case Ast.BinOp(rel_op: RelOp, left, right) => {
                (left, right) match {
                    case (Ast.Var(leftId), Ast.Var(rightId)) => {
                        var rel = Rel.Std(leftId + stack_ptr, rightId + stack_ptr, rel_op)
                        var rel_set = ArrayBuffer(rel)
                        RelSets(ArrayBuffer(rel_set))
                    }
                    case _ => RelSets()
                }
            }
            case call: Ast.Call => {
                var Ast.Call(callee, args) = call
                var Ast.Func(caller_ast, caller_vars) = fn
                var Ast.Func(callee_ast, callee_vars) = callee

                callee == fn match {
                    case true => {
                        var rel_set = ArrayBuffer(Rel.Rec(call))
                        RelSets(ArrayBuffer(rel_set))
                    }
                    case false => {
                        vars.addAll(callee_vars)
                        var new_stack_ptr = stack_ptr + caller_vars.length
                        var rel_sets = Compiler(callee, vars, new_stack_ptr).compile()

                        var arg_binds: RelSet = args.zipWithIndex.map(p => {
                            var (arg_id, i) = p
                            Rel.Std(i + new_stack_ptr, arg_id + stack_ptr, RelOp.Eq)
                        })

                        rel_sets.append_to_all(arg_binds)
                    }
                }
            }
        }
    }
}