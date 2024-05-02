package lib.unifier
import lib.types.*
import lib.solver.*
import lib.compiler.*
import scala.collection.mutable.ArrayBuffer

class Unifier(
    val fn: Ast.Func,
    val args: VarSet
) {
    def unify(): Unit = {
        var var_cpy = fn.vars.clone()
        args.zipWithIndex.foreach(p => var_cpy(p._2) = p._1)

        var complier = Compiler(fn, var_cpy)
        var rs: RelSets = complier.compile()
        var sat_set = rs.sort().find_sat_set(complier.vars)

        sat_set match {
            case Some(p) => {
                var (rels, vars) = p
                println(fn.name + "(" + vars.take(args.length) + ")")
                println("Satisfiable.\n")
            }
            case None => {
                println(fn.name + "(" + var_cpy + ")")
                println("Not satisfiable.\n")
            }
        }
    }
}