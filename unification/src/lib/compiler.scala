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
            case Ast.Var(id) => RelSets.Empty
            case Ast.Func(_, ast, vars) => compile(ast)
            case call: Ast.Call => RelSets.SingleRel(Rel.Rec(fn, call))
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
                        RelSets.SingleRel(rel)
                    }
                    case _ => RelSets.Empty
                }
            }
        }
    }
}