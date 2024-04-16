file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
### dotty.tools.dotc.core.TypeError$$anon$1: Toplevel definition VarId is defined in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
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
offset: 72
uri: file:///C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
text:
```scala
package lib.unify
import scala.collection.mutable.ArrayBuffer
import l@@

class Env(val vars: ArrayBuffer[Var], val stack_ptr: Int, val caller: AST) {
    def applied(ties: ArrayBuffer[Tie]): Env = {
        var new_vars = vars.clone()

        var let_me_out = false
        while (!let_me_out) {
            let_me_out = true
            for (tie <- ties) {
                var (to, from) = tie
                (new_vars(to), vars(from)) match {
                    case (Some(x), Some(y)) where x != y => {
                        let_me_out = false
                        e(to) = vars(from)
                    }
                    _ => {}
                }
            }
        }

        Env(new_vars)
    }
    
    def shifted(var_cnt: Int): Env = {
        var AST.Func(_, _, var_cnt) = caller: @unchecked
        var new_vars = vars.clone()
        Range(0, var_cnt).foreach(_ => new_vars.addOne(Option.empty))
        Env(new_vars, stack_ptr + var_cnt)
    }

    def print(ties: ArrayBuffer[Tie]): Unit = {
        // ima take a shit, shit shit.
    }

    def get_var(idx: Int): Var = vars(stack_ptr + idx)
    def set_var(idx: Int, v: Var): Unit = vars(stack_ptr + idx) = v
}

object Union {
    def unify(ast: AST, env: Env): (Boolean, ArrayBuffer[Tie]) = { // return (satisfiable, ties/reqs)
        ast match {
            case AST.BinOp(OP.And, left, right) => {
                val (leftSat, leftTies) = unify(left, env)
                leftSat match {
                    case true => {
                        val (rightSat, rightTies) = unify(right, env.applied(leftTies))
                        rightSat match {
                            case true  => (true, leftTies ++ rightTies)
                            case false => (false, ArrayBuffer())
                        }
                    }
                    case false => (false, ArrayBuffer())
                }
            }
            case AST.BinOp(OP.Or, left, right) => {
                val (leftSat, leftTies) = unify(left, env)
                val (rightSat, rightTies) = unify(right, env)
                (leftSat, rightSat) match {
                    case (false, false) => (false, ArrayBuffer())
                    case (true, _)      => (true, leftTies)
                    case (_, true)      => (true, rightTies)
                }
            }
            case AST.BinOp(OP.Eq, left, right) => {
                val AST.Var(leftId) = left: @unchecked
                val AST.Var(rightId) = right: @unchecked
                (env.get_var(leftId), env.get_var(rightId)) match {
                    case (Some(x), Some(y)) => (x == y, ArrayBuffer())                                   // value = value
                    case (None, None)       => (true, ArrayBuffer((leftId, rightId), (rightId, leftId))) // symbol = symbol
                    case (None, _)          => (true, ArrayBuffer((leftId, rightId)))                    // symbol = value
                    case (_, None)          => (true, ArrayBuffer((rightId, leftId)))                    // value = symbol
                }
            }
            case AST.Var(id) => (true, ArrayBuffer())
            case AST.Func(ast, args, var_cnt) => {
                var new_env: Env = env.shifted(var_cnt)
                var new_ties: ArrayBuffer[Tie] = ArrayBuffer()
                for (i <- 0 to args.length) {
                    var arg_idx = args(i)
                    var base_idx = i + env.stack_ptr
                    new_ties.addAll(((arg_idx, base_idx), (base_idx, arg_idx)))
                }

                var (sat, ties) = unify(ast, new_env)
                (sat, ties ++ new_ties)
            }
            case _ => (false, ArrayBuffer())
        }
    }
}
```



#### Error stacktrace:

```

```
#### Short summary: 

dotty.tools.dotc.core.TypeError$$anon$1: Toplevel definition VarId is defined in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/unify.scala
and also in
  C:/Users/Arnav/Documents/GitHub/unification/unification/src/lib/types.scala
One of these files should be removed from the classpath.