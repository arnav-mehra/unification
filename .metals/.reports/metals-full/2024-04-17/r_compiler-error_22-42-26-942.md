file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/solver.scala
### java.lang.AssertionError: NoDenotation.owner

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.3
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.3\scala3-library_3-3.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar [exists ]
Options:



action parameters:
offset: 1343
uri: file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/solver.scala
text:
```scala
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
    def apply_rel(rel: Rel): Status = {
        var Rel.Std(var_id1: VarId, var_id2: VarId, rel_op: RelOp) = rel: @unchecked

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

    def solve_recursive(rec_rel: Rel.Rec): Boolean = {
        var Rel.Rec(call) = rec_rel
        var Ast.Call(callee, args) = call
        var Ast.Func(_, new_vars) = callee

        vars.addAll(new_vars)
        var new_stack_ptr = vars.length - new_vars.length
        var old_stack_ptr = new_stack_ptr - new_vars.length
        var new_rel_sets = Compiler(callee, vars, new_stack_ptr).compile()

        var arg_bind_rel_set: RelSet = Range[]@@(0, args.length).toBuffer.map(i => {
            Rel.Std(i + new_stack_ptr, args(i) + old_stack_ptr, RelOp.Eq)
        })

        new_rel_sets.append_to_all(rels ++ arg_bind_rel_set)
                    .find_sat_set(vars)
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
        var rec_rel_idx = rels.indexWhere(_ match {
            case Rel.Rec(_) => true
            case _ => false
        })
        rec_rel_idx match {
            case -1 => solve_direct()
            case idx => {
                var rec_rel: Rel.Rec = rels(idx).asInstanceOf[Rel.Rec]: @unchecked
                rels.remove(idx, 1)
                solve_recursive(rec_rel)
            }
        }
    }
}
```



#### Error stacktrace:

```
dotty.tools.dotc.core.SymDenotations$NoDenotation$.owner(SymDenotations.scala:2607)
	scala.meta.internal.pc.SignatureHelpProvider$.isValid(SignatureHelpProvider.scala:83)
	scala.meta.internal.pc.SignatureHelpProvider$.notCurrentApply(SignatureHelpProvider.scala:96)
	scala.meta.internal.pc.SignatureHelpProvider$.$anonfun$1(SignatureHelpProvider.scala:48)
	scala.collection.StrictOptimizedLinearSeqOps.dropWhile(LinearSeq.scala:280)
	scala.collection.StrictOptimizedLinearSeqOps.dropWhile$(LinearSeq.scala:278)
	scala.collection.immutable.List.dropWhile(List.scala:79)
	scala.meta.internal.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:48)
	scala.meta.internal.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:414)
```
#### Short summary: 

java.lang.AssertionError: NoDenotation.owner