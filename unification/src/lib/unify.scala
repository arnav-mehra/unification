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
}

class Unify(
    val vars: VarSet,
    val stack_ptr: VarId,
    val caller: Ast
) {
    def unify(): RelSets = unify(caller)

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
            case Ast.Func(ast, args, var_cnt) => {
                RelSets(ArrayBuffer()) // to redo
            }
            case _ => {
                RelSets(ArrayBuffer())
            }
        }
    }
}