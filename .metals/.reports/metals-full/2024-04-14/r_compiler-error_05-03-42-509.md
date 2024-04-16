file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/main/scala/Main.scala
### dotty.tools.dotc.core.TypeError$$anon$1: Toplevel definition Var is defined in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/main/scala/Main.scala
and also in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
One of these files should be removed from the classpath.

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.1
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.1\scala3-library_3-3.3.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.10\scala-library-2.13.10.jar [exists ]
Options:



action parameters:
offset: 29
uri: file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/main/scala/Main.scala
text:
```scala
package Main

import scala.@@collection.mutable.ArrayBuffer

object Main {
  def main(args: Array[String]): Unit = {
    var SameLen = AST.BinOp(
      OP.And,
      AST.Eq(AST.Var(0), AST.Var(1))
    )
    var vars: Buffer[Var] = { Option(5); None } 
    var res = Union.unify(SameLen, vars)
    print(res)
  }
}
```



#### Error stacktrace:

```

```
#### Short summary: 

dotty.tools.dotc.core.TypeError$$anon$1: Toplevel definition Var is defined in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/main/scala/Main.scala
and also in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
One of these files should be removed from the classpath.