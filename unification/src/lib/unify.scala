package lib.unify
import lib.types.*
import scala.collection.mutable.ArrayBuffer

enum Status {
    case NoChange
    case Change
    case NoSat
}

class Solver(vars: VarSet, rels: RelSet) {
    def apply_rel(rel: Rel): Status = {
        var (var_id1: VarId, var_id2: VarId, rel_op: RelOp) = rel

        (vars(var_id1), vars(var_id2)) match {
            case (Some(v1), Some(v2)) => {
                if (v1 == v2) Status.NoChange else Status.NoSat
            }
            case (None, Some(v2)) => {
                vars(var_id1) = Some(rel_op.fw(v2))
                Status.Change
            }
            case (Some(v1), None) => {
                vars(var_id2) = Some(rel_op.bw(v1))
                Status.Change
            }
            case _ => Status.NoChange
        }
    }

    def solve(): Boolean = {
        var sat: Option[Boolean] = Option.empty
        
        while (sat.isEmpty) {
            var change = false
            for (rel <- rels) {
                apply_rel(rel) match {
                    case Status.NoSat => sat = Some(false)
                    case Status.Change => change = true
                    case Status.NoChange => {}
                }
            }
            if (sat.isEmpty && !change) {
                sat = Some(true)
            }
        }

        var Some(b) = sat: @unchecked
        b
    }
}

class RelSets(val sets: ArrayBuffer[RelSet]) {
    def and(rss2: RelSets): RelSets = {
        val new_sets: ArrayBuffer[RelSet] = ArrayBuffer()
        for (set <- sets) {
            for (set2 <- rss2.sets) {
                new_sets.addOne(set ++ set2)
            }
        }
        RelSets(new_sets)
    }

    def or(rss2: RelSets): RelSets = {
        RelSets(sets ++ rss2.sets)
    }

    def append_to_all(new_set: RelSet): Unit = {
        sets.foreach(_.appendAll(new_set))
    }
}

class Unifier(
    val vars: VarSet,
    val stack_ptr: VarId,
    val callee: Ast
) {
    def unify(): RelSets = unify(callee)

    def unify(ast: Ast): RelSets = {
        ast match {
            case Ast.BinOp(log_op: LogOp, left, right) => {
                var leftRels = unify(left)
                var rightRels = unify(right)
                log_op match {
                    case LogOp.And => leftRels.and(rightRels)
                    case LogOp.Or => leftRels.or(rightRels)
                }
            }
            case Ast.BinOp(rel_op: RelOp, left, right) => {
                (left, right) match {
                    case (Ast.Var(leftId), Ast.Var(rightId)) => {
                        var rel: Rel = (leftId + stack_ptr, rightId + stack_ptr, rel_op)
                        var rel_set: RelSet = ArrayBuffer(rel)
                        RelSets(ArrayBuffer(rel_set))
                    }
                    case _ => RelSets(ArrayBuffer())
                }
            }
            case Ast.Func(callee, args, var_cnt) => {
                var rel: Rel = (Int.MaxValue, Int.MaxValue, RelOp.Rec)
                RelSets(ArrayBuffer(rel))
            }
            case Ast.Func(ast, args, var_cnt) => {
                var Ast.Func(_, _, caller_var_cnt) = callee: @unchecked
                var new_stack_ptr = stack_ptr + caller_var_cnt
                var new_rel_sets = Unifier(new_stack_ptr, ast).unify()

                var arg_bind_rel_set: RelSet = Range(0, args.length).map(i => {
                    var arg_id = args(i)
                    (i + new_stack_ptr, arg_id + stack_ptr, RelOp.Eq)
                })

                new_rel_sets.append_to_all(arg_bind_rel_set)
                new_rel_sets
            }
            case _ => {
                RelSets(ArrayBuffer())
            }
        }
    }
}