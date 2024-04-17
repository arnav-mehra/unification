package lib.solver
import lib.types.*
import scala.collection.mutable.ArrayBuffer

enum Status {
    case NoChange
    case Change
    case NoSat
}

class Solver(vars: VarSet, rels: RelSet) {
    def apply_rel(rel: Rel): Status = {
        var Rel(var_id1: VarId, var_id2: VarId, rel_op: RelOp) = rel: @unchecked

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

    def solve_recursive(rec_rel: Rel): Boolean = {
        var Rel(var_id1: VarId, var_id2: VarId, caller: Ast) = rec_rel: @unchecked
        var Ast.Call(fn, args) = caller: @unchecked
        var Ast.Func(_, new_vars) = fn: @unchecked

        vars.addAll(new_vars)
        var old_stack_ptr = vars.length - new_vars.length
        var new_stack_ptr = old_stack_ptr - new_vars.length
        var new_rel_sets = Compiler(vars, new_stack_ptr, fn).compile()

        var arg_bind_rel_set: RelSet = Range(0, args.length).map(i => {
            Rel(i + new_stack_ptr, args(i) + old_stack_ptr, RelOp.Eq)
        })

        new_rel_sets.append_to_all(rels ++ arg_bind_rel_set).find_sat_set(vars)
    }

    def solve_direct(): Boolean = {
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

    def solve(): Boolean = {
        var opt_rec_rel = rels.find(_ match {
            case Rel.Rec => true
            case _ => false
        })
        opt_rec_rel match {
            case Some(rec_rel) => solve_recursive(rec_rel)
            case None => solve_direct()
        }
    }
}