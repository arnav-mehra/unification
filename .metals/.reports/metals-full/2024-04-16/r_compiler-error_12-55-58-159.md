file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
### java.lang.IndexOutOfBoundsException: 0

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.1
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.1\scala3-library_3-3.3.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.10\scala-library-2.13.10.jar [exists ]
Options:



action parameters:
offset: 4192
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

    def sort(): Unit = {
        
    }
}

class Compiler(
    val vars: VarSet,
    val stack_ptr: VarId,
    val callee: Ast
) {
    def compile(): RelSets = compile(callee)

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
            case Ast.Func(ast, args, new_vars) => {
                vars.addAll(new_vars)

                var Ast.Func(_, _, caller_var_cnt) = callee: @unchecked
                var new_stack_ptr = stack_ptr + caller_var_cnt
                var new_rel_sets = Compiler(vars, new_stack_ptr, ast).compile()

                var arg_bind_rel_set: RelSet = Range(0, args.length).map(i => {
                    (i + new_stack_ptr, args(i) + stack_ptr, RelOp.Eq)
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

class Unifier(
    val callee: Ast,
    val args: VarSet
) {
    def unify(): Unit = {
        var Ast.Func(_, _, vars) = callee: @unchecked
        Range(0, args.length).foreach(i => vars(i) = args(i))

        var rel_sets = Compiler(vars, 0, callee).compile().sort()
        
        for (@@)
        Solver(vars, r)
    }
}
```



#### Error stacktrace:

```
scala.collection.LinearSeqOps.apply(LinearSeq.scala:131)
	scala.collection.LinearSeqOps.apply$(LinearSeq.scala:128)
	scala.collection.immutable.List.apply(List.scala:79)
	dotty.tools.dotc.util.Signatures$.countParams(Signatures.scala:501)
	dotty.tools.dotc.util.Signatures$.applyCallInfo(Signatures.scala:186)
	dotty.tools.dotc.util.Signatures$.computeSignatureHelp(Signatures.scala:94)
	dotty.tools.dotc.util.Signatures$.signatureHelp(Signatures.scala:63)
	scala.meta.internal.pc.MetalsSignatures$.signatures(MetalsSignatures.scala:17)
	scala.meta.internal.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:51)
	scala.meta.internal.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:398)
```
#### Short summary: 

java.lang.IndexOutOfBoundsException: 0