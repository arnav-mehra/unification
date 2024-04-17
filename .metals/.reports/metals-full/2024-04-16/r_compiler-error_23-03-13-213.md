file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
### java.lang.AssertionError: NoDenotation.owner

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.3
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.3\scala3-library_3-3.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar [exists ]
Options:



action parameters:
offset: 363
uri: file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
text:
```scala
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

        rel_op match {
            case RelOp.Rec(@@) => {
                var Ast.Func(ast, args, new_vars) = fn: @unchecked

                vars.addAll(new_vars)

                var new_stack_ptr = stack_ptr + caller_var_cnt
                var rel_sets = Compiler(vars, new_stack_ptr, ast).compile()

                var arg_bind_rel_set: RelSet = Range(0, args.length).map(i => {
                    (i + new_stack_ptr, args(i) + stack_ptr, RelOp.Eq)
                })

                rel_sets.append_to_all(arg_bind_rel_set)
            }
            case _ => {
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

    def append_to_all(new_set: RelSet): RelSets = {
        sets.foreach(_.appendAll(new_set))
        this
    }

    def sort(): RelSets = {
        sets.sortInPlaceBy(set => {
            if (set.contains(Rel.Rec)) Int.MaxValue else set.length 
        })
        this
    }

    def find_sat_set(fn: Ast, vars: VarSet): RelSet = {
        sets.find(set => Solver(fn, vars.clone(), set).solve())
    }
}

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
```



#### Error stacktrace:

```
dotty.tools.dotc.core.SymDenotations$NoDenotation$.owner(SymDenotations.scala:2607)
	scala.meta.internal.pc.SignatureHelpProvider$.isValid(SignatureHelpProvider.scala:83)
	scala.meta.internal.pc.SignatureHelpProvider$.notCurrentApply(SignatureHelpProvider.scala:92)
	scala.meta.internal.pc.SignatureHelpProvider$.$anonfun$1(SignatureHelpProvider.scala:48)
	scala.collection.StrictOptimizedLinearSeqOps.dropWhile(LinearSeq.scala:280)
	scala.collection.StrictOptimizedLinearSeqOps.dropWhile$(LinearSeq.scala:278)
	scala.collection.immutable.List.dropWhile(List.scala:79)
	scala.meta.internal.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:48)
	scala.meta.internal.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:414)
```
#### Short summary: 

java.lang.AssertionError: NoDenotation.owner