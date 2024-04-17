package lib.unifier
import lib.types.*
import lib.solver.*
import scala.collection.mutable.ArrayBuffer

class Compiler(
    val vars: VarSet,
    val stack_ptr: VarId,
    val fn: Ast
) {
    def compile(): RelSets = compile(fn)

    def compile(ast: Ast): RelSets = {
        ast match {
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
                        var rel = Rel(leftId + stack_ptr, rightId + stack_ptr, rel_op)
                        var rel_set: RelSet = ArrayBuffer(rel)
                        RelSets(ArrayBuffer(rel_set))
                    }
                    case _ => RelSets(ArrayBuffer())
                }
            }
            case Ast.Call(fn, args) => {
                RelSets(ArrayBuffer(Rel.Rec(fn)))
            }
            case Ast.Call(callee, args) => {
                var Ast.Func(caller_ast, caller_vars) = fn: @unchecked
                var Ast.Func(callee_ast, callee_vars) = callee: @unchecked

                vars.addAll(callee_vars)
                var new_stack_ptr = stack_ptr + caller_vars.length
                var rel_sets = Compiler(vars, new_stack_ptr, callee).compile()

                var arg_bind_rel_set: RelSet = Range(0, args.length).map(i => {
                    Rel(i + new_stack_ptr, args(i) + stack_ptr, RelOp.Eq)
                })

                rel_sets.append_to_all(arg_bind_rel_set)
            }
            case _ => {
                RelSets(ArrayBuffer())
            }
        }
    }
}

class Unifier(
    val callee: Ast,
    val args: VarSet
) {
    def unify(): Option[RelSet] = {
        var Ast.Func(_, _, vars) = callee: @unchecked
        args.copyToBuffer(vars)
        var var_cpy = vars.clone()
        
        var complier = Compiler(var_cpy, 0, callee)
        var res = complier.compile()
        res.sort().find_sat_set(complier.vars)
    }
}