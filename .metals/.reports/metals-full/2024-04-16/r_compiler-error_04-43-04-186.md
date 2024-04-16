file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/main.scala
### dotty.tools.dotc.core.TypeError$$anon$1: Toplevel definition Rel is defined in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/types.scala
and also in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/types.scala
One of these files should be removed from the classpath.

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.1
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.1\scala3-library_3-3.3.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.10\scala-library-2.13.10.jar [exists ]
Options:



action parameters:
uri: file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/main.scala
text:
```scala
package main
import lib.types.*
import lib.unify.*
import scala.collection.mutable.ArrayBuffer

object Main {
  def main(args: Array[String]): Unit = {
    var func = Ast.BinOp(LogOp.And,
      Ast.BinOp(
        RelOps.Eq,
        Ast.Var(0),
        Ast.Var(1)
      ),
      Ast.BinOp(
        RelOps.Eq,
        Ast.Var(1),
        Ast.Var(2)
      )
    )
    
    var vars: VarSet = ArrayBuffer(Option.empty, Option.empty, Option.empty)

    var u = Unify(vars, 0, func)
    var sol: RelSets = u.unify()
    sol.sets.foreach(set => {
      var ()
      println(set)
    })
  }
}
```



#### Error stacktrace:

```

```
#### Short summary: 

dotty.tools.dotc.core.TypeError$$anon$1: Toplevel definition Rel is defined in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/types.scala
and also in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/types.scala
One of these files should be removed from the classpath.