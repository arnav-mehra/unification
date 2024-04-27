package lib.solver
import lib.types.*
import lib.unifier.*
import lib.compiler.*
import scala.collection.mutable.ArrayBuffer

enum Status {
    case NoChange
    case Change
    case NoSat
}

class Solver(vars: VarSet, rels: RelSet) {
    // apply a relation and return a status (ignoring recursive relations).
    def apply_rel(rel: Rel): Status = {
        rel match {
            case Rel.Std(vid1, vid2, rel_op) => {
                (vars(vid1), vars(vid2)) match {
                    case (Some(_), Some(x2)) => {
                        vars(vid1) == rel_op.fw(x2) match {
                            case true => Status.NoChange
                            case false => Status.NoSat
                        }
                    }
                    case (None, Some(x2)) => {
                        vars(vid1) = rel_op.fw(x2)
                        vars(vid1) match {
                            case Some(_) => Status.Change
                            case None => Status.NoSat
                        }
                    }
                    case (Some(x1), None) => {
                        vars(vid2) = rel_op.bw(x1)
                        vars(vid2) match {
                            case Some(_) => Status.Change
                            case None => Status.NoSat
                        }
                    }
                    case _ => Status.NoChange
                }
            }
            case _ => Status.NoChange
        }
    }

    // determine if relations are consistent (ignoring recursive relations).
    def solve_direct(): Boolean = {
        var sat: Option[Boolean] = None
        
        while (sat.isEmpty) {
            var change = false
            for (rel <- rels) {
                apply_rel(rel) match {
                    case Status.NoSat => sat = Some(false)
                    case Status.Change => change = true
                    case Status.NoChange => {}
                }
            }
            if (sat.isEmpty && !change) { // relations stabilized => consistency => sat
                sat = Some(true)
            }
        }
        
        sat == Some(true)
    }
    
    // rels has recursive relation, unroll it once and recur.
    def solve_recursive(rec_rel: Rel.Rec): Option[(RelSet, VarSet)] = {
        var Rel.Rec(caller, call) = rec_rel
        var Ast.Call(callee, args) = call
        var Ast.Func(_, _, new_vars) = callee

        // unroll recursive relation.
        vars.addAll(new_vars)
        var new_stack_ptr = vars.length - new_vars.length
        var old_stack_ptr = new_stack_ptr - caller.vars.length
        var new_rel_sets = Compiler(callee, vars, new_stack_ptr).compile()

        // relations to bind arguments/parameters.
        var arg_bind_rel_set: RelSet = args.zipWithIndex.map(p => {
            var (arg_id, i) = p
            Rel.Std(i + new_stack_ptr, arg_id + old_stack_ptr, RelOp.Eq)
        })
        
        // bind unroll relation sets to existing relations and arguments.
        new_rel_sets.prepend_to_all(rels ++ arg_bind_rel_set)
                    .find_sat_set(vars)
    }

    // determine if relations are consistent (including recursive relations).
    def solve(): Option[(RelSet, VarSet)] = {
        solve_direct() match {
            case false => None // inconsistent (even minus any recursive relations).
            case true => {
                var rec_rel_idx = rels.indexWhere(_.isInstanceOf[Rel.Rec])
                rec_rel_idx match {
                    case -1 => Some(rels, vars) // no recursion => sat already known.
                    case idx => { // recursion => check sat after unrolling.
                        var rec_rel: Rel.Rec = rels(idx).asInstanceOf[Rel.Rec]: @unchecked
                        rels.remove(idx, 1)
                        solve_recursive(rec_rel)
                    }
                }
            }
        }
    }
}