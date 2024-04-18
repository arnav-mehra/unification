file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/types.scala
### java.lang.AssertionError: NoDenotation.owner

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.3
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.3\scala3-library_3-3.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar [exists ]
Options:



action parameters:
offset: 1088
uri: file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/types.scala
text:
```scala
package lib.types
import lib.solver.*
import scala.collection.mutable.ArrayBuffer
import scala.runtime.ScalaRunTime

type Var = Option[Int]

type VarId = Int

type VarSet = ArrayBuffer[Var]

enum RelOp(val fw: Int => Int, val bw: Int => Int) {
    case Eq extends RelOp((v: Int) => v, (v: Int) => v)
    case Inc extends RelOp((v: Int) => v + 1, (v: Int) => v - 1)
}

enum LogOp {
    case Or
    case And
}

enum Ast {
    case Var  (id: VarId)
    case BinOp(op: LogOp | RelOp, left: Ast, right: Ast)
    case Func (var ast: Ast, var vars: VarSet)
    case Call (callee: Ast.Func, args: ArrayBuffer[VarId])
}

enum Rel {
    case Rec(call: Ast.Call)
    case Std(left: VarId, right: VarId, rel: RelOp)

    override def toString(): String = {
        var var_names = ArrayBuffer("x", "y", "z", "0", "xc", "yc", "x2", "y2", "z2", "0", "x2c", "y2c")
        this match {
            case Rec(call) => "Rec(" + call.args.foldLeft("")((acc, id) => acc + ", [" + id + "]").substring(2) + ")"
            case Std(left, right, RelOp.Eq) => "[" + var_names[@@left + "] = [" + right + "]"
            case Std(left, right, RelOp.Inc) => "[" + left + "] = [" + right + "] + 1"
        }
    }
}

type RelSet = ArrayBuffer[Rel]

class RelSets(val sets: ArrayBuffer[RelSet] = ArrayBuffer()) {
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

    def find_sat_set(vars: VarSet): Option[RelSet] = {
        sets.find(Solver(vars.clone(), _).solve())
    }

    def print(): Unit = {
        sets.foreach(println)
    }
}
```



#### Error stacktrace:

```
dotty.tools.dotc.core.SymDenotations$NoDenotation$.owner(SymDenotations.scala:2607)
	scala.meta.internal.pc.SignatureHelpProvider$.isValid(SignatureHelpProvider.scala:83)
	scala.meta.internal.pc.SignatureHelpProvider$.notCurrentApply(SignatureHelpProvider.scala:94)
	scala.meta.internal.pc.SignatureHelpProvider$.$anonfun$1(SignatureHelpProvider.scala:48)
	scala.collection.StrictOptimizedLinearSeqOps.dropWhile(LinearSeq.scala:280)
	scala.collection.StrictOptimizedLinearSeqOps.dropWhile$(LinearSeq.scala:278)
	scala.collection.immutable.List.dropWhile(List.scala:79)
	scala.meta.internal.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:48)
	scala.meta.internal.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:414)
```
#### Short summary: 

java.lang.AssertionError: NoDenotation.owner