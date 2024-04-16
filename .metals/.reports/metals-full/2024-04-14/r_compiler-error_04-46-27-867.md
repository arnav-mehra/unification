file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/main/scala/Main.scala
### java.lang.AssertionError: NoDenotation.owner

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.1
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.1\scala3-library_3-3.3.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.10\scala-library-2.13.10.jar [exists ]
Options:



action parameters:
offset: 470
uri: file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/main/scala/Main.scala
text:
```scala
import scala.collection.mutable.ArrayBuffer

enum OP {
  case And
  case Or
  case Eq
}

enum AST {
  case Var(id: Int)
  case BinOp(op: OP, left: AST, right: AST)
  case UnOp(op: OP, child: AST)
  case Func(ast: AST, args: ArrayBuffer[Int])
}

type Var = Option[Int]
type Tie = (to: Int, from: Int)

class Env(val vars: ArrayBuffer[Var]) {
  def applied(ties: ArrayBuffer[Tie]): Env = {
    var e = vars.clone()
    for (tie <- ties) {
      e.vars[@@]
      e.vars[tie.to] = vars[tie.from]
    }
    e
  }
  
  def filtered(Env e, Buffer[Int] idxs): Env = {
    Env(idxs.map(idx => vars[idx]))
  }
}

object Union {
  def unify(ast: AST, env: Env): (Boolean, Buffer[Tie]) = { // return (satisfiable, ties/reqs)
    ast match {
      case AST.BinOp(Op.And, left, right) => {
        val (leftSat, leftTies) = unify(left, env)
        match leftSat {
          true => {
            val (rightSat, rightTies) = unify(right, env.applied(leftTies))
            match leftSat {
              true  => (true, leftTies ++ rightTies)
              false => (false, Buffer())
            }
          }
          false => (false, Buffer())
        }
      }
      case AST.BinOp(Op.Or, left, right) => {
        val (leftSat, leftTies) = unify(left, env)
        val (rightSat, rightTies) = unify(right, env)
        match (leftSat, rightSat) => {
          (false, false) => (false, Buffer())
          (true, _)      => (true, leftTies)
          (_, true)      => (true, rightTies)
        }
      }
      case AST.BinOp(Op.Eq, left, right) => {
        val AST.Var(leftId) = left: @unchecked
        val AST.Var(rightId) = right: @unchecked
        match (env[leftId], env[rightId]) {
          (Option(x), Option(y)) => (x == y, Buffer())                // value = value
          (_, _)             => (true, Buffer()[leftId, rightId]) // symbol = value, symbol = symbol
        }
      }
      case AST.Var(id) => (true, Buffer())
      case AST.Func(ast, args) => {
        unify(ast, env.filtered(args))
      }
      _ => (false, Buffer())
    }
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    var SameLen = AST.BinOp(
      OP.And,
      AST.Eq(AST.Var(0), AST.Var(1))
    )
    var vars: Buffer[Var] = { Option(5), None } 
    var res = Union.unify(SameLen, vars)
    print(res)
  }
}
```



#### Error stacktrace:

```
dotty.tools.dotc.core.SymDenotations$NoDenotation$.owner(SymDenotations.scala:2582)
	scala.meta.internal.pc.SignatureHelpProvider$.isValid(SignatureHelpProvider.scala:83)
	scala.meta.internal.pc.SignatureHelpProvider$.notCurrentApply(SignatureHelpProvider.scala:96)
	scala.meta.internal.pc.SignatureHelpProvider$.$anonfun$1(SignatureHelpProvider.scala:48)
	scala.collection.StrictOptimizedLinearSeqOps.loop$3(LinearSeq.scala:280)
	scala.collection.StrictOptimizedLinearSeqOps.dropWhile(LinearSeq.scala:282)
	scala.collection.StrictOptimizedLinearSeqOps.dropWhile$(LinearSeq.scala:278)
	scala.collection.immutable.List.dropWhile(List.scala:79)
	scala.meta.internal.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:48)
	scala.meta.internal.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:398)
```
#### Short summary: 

java.lang.AssertionError: NoDenotation.owner