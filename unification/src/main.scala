package main
import lib.types.*
import lib.unify.*
import scala.collection.mutable.ArrayBuffer

object Main {
  def main(args: Array[String]): Unit = {
    var func = Ast.BinOp(LogOp.And,
      Ast.BinOp(LogOp.Or,
        Ast.BinOp(
          RelOp.Eq,
          Ast.Var(0),
          Ast.Var(1)
        ),
        Ast.BinOp(
          RelOp.Eq,
          Ast.Var(1),
          Ast.Var(2)
        )
      ),
      Ast.BinOp(LogOp.Or,
        Ast.BinOp(
          RelOp.Eq,
          Ast.Var(1),
          Ast.Var(0)
        ),
        Ast.BinOp(
          RelOp.Eq,
          Ast.Var(2),
          Ast.Var(1)
        )
      )
    )
    
    var vars: VarSet = ArrayBuffer(Option.empty, Option.empty, Option.empty)

    var u = Unifier(vars, 0, func)
    var sol: RelSets = u.unify()
    sol.sets.foreach(println)
  }
}