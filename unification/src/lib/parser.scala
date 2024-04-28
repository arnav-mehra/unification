package lib.parser

import scala.util.parsing.combinator.JavaTokenParsers
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import lib.types.*

enum ParsedAst {
    case BinOp(op: LogOp | RelOp, left: ParsedAst, right: ParsedAst)
    case Var(name: ParsedVar)
    case Func(name: String, var ast: ParsedAst, var params: ArrayBuffer[ParsedVar])
    case Call(callee: String, args: ArrayBuffer[ParsedVar])
}

enum ParsedVar {
    case Symbol(name: String)
    case Number(value: Int)
}

type VarMap = HashMap[ParsedVar, VarId]

class Indexer(fns: ArrayBuffer[ParsedAst.Func], qs: ArrayBuffer[ParsedAst.Call]) {
    val fn_map: HashMap[String, (Ast.Func, VarMap)] = HashMap()

    def digest: (ArrayBuffer[Ast.Func], ArrayBuffer[Ast.Query]) = {
        fns.foreach(fn => {
            var var_map = VariableIndexer(fn).digest
            println(var_map)
            var new_vars: VarSet = var_map.map(_ => None).to(ArrayBuffer)
            var_map.foreach((pvar, id) => pvar match {
                case ParsedVar.Number(v) => new_vars(id) = Some(v)
                case _ => {}
            })
            var fn_ast: Ast.Func = Ast.Func(fn.name, Ast.Var(0), new_vars)
            fn_map.addOne(fn.name, (fn_ast, var_map))
        })

        var new_fns = fns.map(pfn => {
            var (fn, var_map) = fn_map(pfn.name)
            fn.ast = rebuild(pfn.ast, var_map)
            fn
        })
        var new_qs: ArrayBuffer[Ast.Query] = qs.map(pq => {
            var (fn, _) = fn_map(pq.callee)
            var var_args: VarSet = pq.args.map {
                case ParsedVar.Symbol(s) => None
                case ParsedVar.Number(x) => Some(x)
            }
            Ast.Query(fn, var_args)
        })

        (new_fns, new_qs)
    }
    
    def rebuild(ast: ParsedAst, var_map: VarMap): Ast = {
        ast match {
            case ParsedAst.BinOp(op, left, right) => {
                var new_left = rebuild(left, var_map)
                var new_right = rebuild(right, var_map)
                Ast.BinOp(op, new_left, new_right)
            }
            case ParsedAst.Call(callee, args) => {
                var (new_callee, _) = fn_map(callee)
                var new_args = args.map(v => var_map(v))
                Ast.Call(new_callee, new_args)
            }
            case ParsedAst.Var(pvar) => {
                Ast.Var(var_map(pvar))
            }
            case _ => Ast.Var(0)
        }
    }
}

class VariableIndexer(fn_ast: ParsedAst.Func) {
    val var_map: VarMap = HashMap()

    def get_var(pvar: ParsedVar): VarId = {
        if (!var_map.contains(pvar)) {
            var_map.addOne(pvar, var_map.size) 
        }
        var_map(pvar)
    }

    def digest: VarMap = {
        recur_index_vars(fn_ast)
        var_map
    }

    def recur_index_vars(ast: ParsedAst): Unit = {
        ast match {
            case ParsedAst.Var(pvar) => {
                get_var(pvar)
            }
            case ParsedAst.Func(name, ast, params) => {
                params.foreach(pvar => get_var(pvar))
                recur_index_vars(ast)
            }
            case ParsedAst.Call(name, args) => {
                args.foreach(pvar => get_var(pvar))
            }
            case ParsedAst.BinOp(op, left, right) => {
                recur_index_vars(left)
                recur_index_vars(right)
            }
        }   
    }
}

object ProgramParser extends JavaTokenParsers {
    def parse(code: String): (ArrayBuffer[ParsedAst.Func], ArrayBuffer[ParsedAst.Call]) = {
        parseAll(lines, code).get
    }

    def lines: Parser[(ArrayBuffer[ParsedAst.Func], ArrayBuffer[ParsedAst.Call])] = rep((func_dec | query) ~ ";") ^^ {
        s => {
            var ls = s.map(s => s._1).to(ArrayBuffer)
            (
                ls.collect { case a: ParsedAst.Func => a },
                ls.collect { case a: ParsedAst.Call => a }
            )
        }
    }

    def query: Parser[ParsedAst.Call] = call_ast

    def func_dec: Parser[ParsedAst.Func] = func_name ~ rep(variable) ~ "=>" ~ expr_ast ^^ {
        case fname~ls~"=>"~ast => ParsedAst.Func(fname, ast, ls.to(ArrayBuffer))
    }

    def expr_ast: Parser[ParsedAst] = (log_ast | rel_ast | call_ast)

    def log_ast: Parser[ParsedAst] = "(" ~ expr_ast ~ rep(log_op ~ expr_ast) ~ ")" ^^ {
        case "("~left~ls~")" => {
            ls.foldLeft(left)((acc, right) =>
                ParsedAst.BinOp(right._1, acc, right._2)
            )
        }
    }

    def rel_ast: Parser[ParsedAst.BinOp] = var_ast ~ rel_op ~ var_ast ^^ {
        case vl~op~vr => ParsedAst.BinOp(op, vl, vr)
    }

    def call_ast: Parser[ParsedAst.Call] = func_name ~ rep(variable) ^^ {
        case fn_name~arg_ls => ParsedAst.Call(fn_name, arg_ls.to(ArrayBuffer))
    }

    def log_op: Parser[LogOp] = ("|" | "&") ^^ {
        case "|" => LogOp.Or
        case "&" => LogOp.And
    }

    def rel_op: Parser[RelOp] = ("=" | ">") ^^ {
        case "=" => RelOp.Eq
        case ">" => RelOp.Inc
    }

    def var_ast: Parser[ParsedAst.Var] = variable ^^ { ParsedAst.Var(_) }
    
    def func_name: Parser[String] = var_name

    def variable: Parser[ParsedVar] =( number | symbol)
    def symbol: Parser[ParsedVar.Symbol] = var_name ^^ { ParsedVar.Symbol(_) }
    def number: Parser[ParsedVar.Number] = "\\d+".r ^^ { s => ParsedVar.Number(s.toInt) }
    
    def var_name: Parser[String] = "[a-zA-Z_][a-zA-Z_0-9]*".r
}