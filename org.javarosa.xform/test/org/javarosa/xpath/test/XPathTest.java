package org.javarosa.xpath.test;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;

import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;


public class XPathTest extends TestCase {
	public static String[][] parseTestCases = {	
		{"", null},
		{"     ", null},
		{"  \t \n  \r ", null},
		{"10", "{num:10.0}"},
		{"123.", "{num:123.0}"},
		{"734.04", "{num:734.04}"},
		{"0.12345", "{num:0.12345}"},
		{".666", "{num:0.666}"},
		{"00000333.3330000", "{num:333.333}"},
		{"0", "{num:0.0}"},
		{"0.", "{num:0.0}"},
		{".0", "{num:0.0}"},
		{"0.0", "{num:0.0}"},
		{"\"\"", "{str:''}"},
		{"\"   \"", "{str:'   '}"},
		{"''", "{str:''}"},
		{"'\"'", "{str:'\"'}"},
		{"\"'\"", "{str:'''}"},
		{"'mary had a little lamb'", "{str:'mary had a little lamb'}"},
		{"'unterminated string...", null},
		{"$var", "{var:var}"},
		{"$qualified:name", "{var:qualified:name}"},
		{"$x:*", null},
		{"$", null},
		{"$$asdf", null},
		{"(5)", "{num:5.0}"},
		{"(( (( (5 )) )))  ", "{num:5.0}"},
		{")", null},
		{"(", null},
		{"()", null},
		{"(((3))", null},
		{"(5)", "{num:5.0}"},
		{"5 + 5", "{binop-expr:+,{num:5.0},{num:5.0}}"},
		{"-5", "{unop-expr:num-neg,{num:5.0}}"},
		{"- 5", "{unop-expr:num-neg,{num:5.0}}"},
		{"----5", "{unop-expr:num-neg,{unop-expr:num-neg,{unop-expr:num-neg,{unop-expr:num-neg,{num:5.0}}}}}"},
		{"6 * - 7", "{binop-expr:*,{num:6.0},{unop-expr:num-neg,{num:7.0}}}"},
		{"0--0", "{binop-expr:-,{num:0.0},{unop-expr:num-neg,{num:0.0}}}"},		
		{"+-", null},
		{"5 * 5", "{binop-expr:*,{num:5.0},{num:5.0}}"},
		{"5 div 5", "{binop-expr:/,{num:5.0},{num:5.0}}"},
		{"5/5", null},
		{"5 mod 5", "{binop-expr:%,{num:5.0},{num:5.0}}"},
		{"5%5", null},
		{"3mod4", "{binop-expr:%,{num:3.0},{num:4.0}}"},
		{"5 divseparate-token", "{binop-expr:/,{num:5.0},{path-expr:rel,{{step:child,separate-token}}}}"}, //not quite sure if this is legal xpath or not, but it *can* be parsed unambiguously
		{"5 = 5", "{binop-expr:==,{num:5.0},{num:5.0}}"},
		{"5 != 5", "{binop-expr:!=,{num:5.0},{num:5.0}}"},
		{"5 == 5", null},
		{"5 <> 5", null},
		{"5 < 5", "{binop-expr:<,{num:5.0},{num:5.0}}"},
		{"5 <= 5", "{binop-expr:<=,{num:5.0},{num:5.0}}"},
		{"5 > 5", "{binop-expr:>,{num:5.0},{num:5.0}}"},
		{"5 >= 5", "{binop-expr:>=,{num:5.0},{num:5.0}}"},
		{">=", null},
		{"'asdf'!=", null},
		{"5 and 5", "{binop-expr:and,{num:5.0},{num:5.0}}"},
		{"5 or 5", "{binop-expr:or,{num:5.0},{num:5.0}}"},
		{"5 | 5", "{binop-expr:union,{num:5.0},{num:5.0}}"},
		{"1 or 2 or 3", "{binop-expr:or,{num:1.0},{binop-expr:or,{num:2.0},{num:3.0}}}"},
		{"1 and 2 and 3", "{binop-expr:and,{num:1.0},{binop-expr:and,{num:2.0},{num:3.0}}}"},
		{"1 = 2 != 3 != 4 = 5", "{binop-expr:==,{binop-expr:!=,{binop-expr:!=,{binop-expr:==,{num:1.0},{num:2.0}},{num:3.0}},{num:4.0}},{num:5.0}}"},
		{"1 < 2 >= 3 <= 4 > 5", "{binop-expr:>,{binop-expr:<=,{binop-expr:>=,{binop-expr:<,{num:1.0},{num:2.0}},{num:3.0}},{num:4.0}},{num:5.0}}"},
		{"1 + 2 - 3 - 4 + 5", "{binop-expr:+,{binop-expr:-,{binop-expr:-,{binop-expr:+,{num:1.0},{num:2.0}},{num:3.0}},{num:4.0}},{num:5.0}}"},
		{"1 mod 2 div 3 div 4 * 5", "{binop-expr:*,{binop-expr:/,{binop-expr:/,{binop-expr:%,{num:1.0},{num:2.0}},{num:3.0}},{num:4.0}},{num:5.0}}"},
		{"1|2|3", "{binop-expr:union,{binop-expr:union,{num:1.0},{num:2.0}},{num:3.0}}"},
		{"1 < 2 = 3 > 4 and 5 <= 6 != 7 >= 8 or 9 and 10",
			"{binop-expr:or,{binop-expr:and,{binop-expr:==,{binop-expr:<,{num:1.0},{num:2.0}},{binop-expr:>,{num:3.0},{num:4.0}}},{binop-expr:!=,{binop-expr:<=,{num:5.0},{num:6.0}},{binop-expr:>=,{num:7.0},{num:8.0}}}},{binop-expr:and,{num:9.0},{num:10.0}}}"},
		{"1 * 2 + 3 div 4 < 5 mod 6 | 7 - 8",
			"{binop-expr:<,{binop-expr:+,{binop-expr:*,{num:1.0},{num:2.0}},{binop-expr:/,{num:3.0},{num:4.0}}},{binop-expr:-,{binop-expr:%,{num:5.0},{binop-expr:union,{num:6.0},{num:7.0}}},{num:8.0}}}"},
		{"- 4 * 6", "{binop-expr:*,{unop-expr:num-neg,{num:4.0}},{num:6.0}}"},
		{"8|-9", null}, //disallowed by the xpath spec
		{"6*(3+4)and(5or2)", "{binop-expr:and,{binop-expr:*,{num:6.0},{binop-expr:+,{num:3.0},{num:4.0}}},{binop-expr:or,{num:5.0},{num:2.0}}}"},
		{"function()", "{func-expr:function,{}}"},
		{"func:tion()", "{func-expr:func:tion,{}}"},
		{"function(   )", "{func-expr:function,{}}"},
		{"function (5)", "{func-expr:function,{{num:5.0}}}"},
		{"function   ( 5, 'arg', 4 * 12)", "{func-expr:function,{{num:5.0},{str:'arg'},{binop-expr:*,{num:4.0},{num:12.0}}}}"},
		{"function ( 4, 5, 6 ", null},
		{"4andfunc()", "{binop-expr:and,{num:4.0},{func-expr:func,{}}}"},		
		{"node()", "{path-expr:rel,{{step:child,node()}}}"},
		{"text()", "{path-expr:rel,{{step:child,text()}}}"},
		{"comment()", "{path-expr:rel,{{step:child,comment()}}}"},
		{"processing-instruction()", "{path-expr:rel,{{step:child,proc-instr()}}}"},
		{"processing-instruction('asdf')", "{path-expr:rel,{{step:child,proc-instr('asdf')}}}"},
		{"node(5)", null},
		{"text('str')", null},
		{"comment(name)", null},
		{"processing-instruction(5)", null},
		{"processing-instruction('asdf','qwer')", null},
		{"bunch-o-nodes()[3]", "{filt-expr:{func-expr:bunch-o-nodes,{}},{{num:3.0}}}"},
		{"bunch-o-nodes()[3]['predicates'!='galore']", "{filt-expr:{func-expr:bunch-o-nodes,{}},{{num:3.0},{binop-expr:!=,{str:'predicates'},{str:'galore'}}}}"},
		{".", "{path-expr:rel,{{step:self,node()}}}"},
		{"..", "{path-expr:rel,{{step:parent,node()}}}"},
		{"name", "{path-expr:rel,{{step:child,name}}}"},
		{"qual:name", "{path-expr:rel,{{step:child,qual:name}}}"},
		{"namespace:*", "{path-expr:rel,{{step:child,namespace:*}}}"},
		{"*", "{path-expr:rel,{{step:child,*}}}"},
		{"*****", "{binop-expr:*,{binop-expr:*,{path-expr:rel,{{step:child,*}}},{path-expr:rel,{{step:child,*}}}},{path-expr:rel,{{step:child,*}}}}"},
		{"_rea--ll:y.funk..y_N4M3", "{path-expr:rel,{{step:child,_rea--ll:y.funk..y_N4M3}}}"},
		{"a:b:c", null},
		{"inv#lid_N~AME", null},
		{".abc", null},
		{"5abc", null},
		{"child::*", "{path-expr:rel,{{step:child,*}}}"},
		{"parent::*", "{path-expr:rel,{{step:parent,*}}}"},
		{"descendant::*", "{path-expr:rel,{{step:descendant,*}}}"},
		{"ancestor::*", "{path-expr:rel,{{step:ancestor,*}}}"},
		{"following-sibling::*", "{path-expr:rel,{{step:following-sibling,*}}}"},
		{"preceding-sibling::*", "{path-expr:rel,{{step:preceding-sibling,*}}}"},
		{"following::*", "{path-expr:rel,{{step:following,*}}}"},
		{"preceding::*", "{path-expr:rel,{{step:preceding,*}}}"},
		{"attribute::*", "{path-expr:rel,{{step:attribute,*}}}"},
		{"namespace::*", "{path-expr:rel,{{step:namespace,*}}}"},
		{"self::*", "{path-expr:rel,{{step:self,*}}}"},
		{"descendant-or-self::*", "{path-expr:rel,{{step:descendant-or-self,*}}}"},
		{"ancestor-or-self::*", "{path-expr:rel,{{step:ancestor-or-self,*}}}"},
		{"bad-axis::*", null},
		{"::*", null},
		{"child::.", null},
		{"..[4]", null},
		{"@attr", "{path-expr:rel,{{step:attribute,attr}}}"},
		{"@*", "{path-expr:rel,{{step:attribute,*}}}"},
		{"@ns:*", "{path-expr:rel,{{step:attribute,ns:*}}}"},
		{"@attr::*", null},
		{"child::func()", null},
		{"descendant::node()[@attr='blah'][4]", "{path-expr:rel,{{step:descendant,node(),{{binop-expr:==,{path-expr:rel,{{step:attribute,attr}}},{str:'blah'}},{num:4.0}}}}}"},
		{"rel/ative/path", "{path-expr:rel,{{step:child,rel},{step:child,ative},{step:child,path}}}"},
		{"rel/ative/path/", null},
		{"/abs/olute/path['etc']", "{path-expr:abs,{{step:child,abs},{step:child,olute},{step:child,path,{{str:'etc'}}}}}"},
		{"filter()/expr/path", "{path-expr:{filt-expr:{func-expr:filter,{}},{}},{{step:child,expr},{step:child,path}}}"},
		{"/", "{path-expr:abs,{}}"},
		{"//", null},
		{"//all", "{path-expr:abs,{{step:descendant-or-self,node()},{step:child,all}}}"},
		{"a/.//../z", "{path-expr:rel,{{step:child,a},{step:self,node()},{step:descendant-or-self,node()},{step:parent,node()},{step:child,z}}}"},

		{"/patient/sex = 'male' and /patient/age > 15",
			"{binop-expr:and,{binop-expr:==,{path-expr:abs,{{step:child,patient},{step:child,sex}}},{str:'male'}},{binop-expr:>,{path-expr:abs,{{step:child,patient},{step:child,age}}},{num:15.0}}}"},
		{"../jr:hist-data/labs[@type=\"cd4\"]",
			"{path-expr:rel,{{step:parent,node()},{step:child,jr:hist-data},{step:child,labs,{{binop-expr:==,{path-expr:rel,{{step:attribute,type}}},{str:'cd4'}}}}}}"},
		{"function_call(26*(7+3), //*, /im/child::an/ancestor::x[3][true()]/path)",
			"{func-expr:function_call,{{binop-expr:*,{num:26.0},{binop-expr:+,{num:7.0},{num:3.0}}},{path-expr:abs,{{step:descendant-or-self,node()},{step:child,*}}},{path-expr:abs,{{step:child,im},{step:child,an},{step:ancestor,x,{{num:3.0},{func-expr:true,{}}}},{step:child,path}}}}}"}			
	};
	
	public XPathTest(String name, TestMethod rTestMethod) {
		super(name, rTestMethod);
	}
	
	public XPathTest(String name) {
		super(name);
	}
	
	public XPathTest() {
		super();
	}	
	
	public Test suite() {
		TestSuite aSuite = new TestSuite();
		
		for (int i = 0; i < parseTestCases.length; i++) {
			final String expr = parseTestCases[i][0];
			final String expected = parseTestCases[i][1];
			
			aSuite.addTest(new XPathTest("XPath Parsing Test [" + expr + "]", new TestMethod() {
				public void run (TestCase tc) {
					((XPathTest)tc).testParse(expr, expected);
				}
			}));
		}
		
		return aSuite;
	}
	
	private void testXPathValid (String expr, String expected) {
		//debug
		//System.out.println("+[ " + expr + " ]");
		
		try {
			XPathExpression xpe = XPathParseTool.parseXPath(expr);
			String result = (xpe != null ? xpe.toString() : null);
			
			//debug
			//System.out.println("[ " + result + " ]");
			
			if (result == null || !result.equals(expected)) {
				this.fail("XPath Parse Failed! Incorrect parse tree." +
							"\n    expression:[" + expr + "]" +
							"\n    expected:[" + expected + "]" +
							"\n    result:[" + result + "]");
			}
		} catch (XPathSyntaxException xse) {
			this.fail("XPath Parse Failed! Unexpected syntax error." + 
						"\n    expression:[" + expr + "]");
		}
	}
	
	private void testXPathInvalid (String expr) {
		//debug
		//System.out.println("+[ " + expr + " ]");
		
		try {
			XPathExpression xpe = XPathParseTool.parseXPath(expr);
			String result = (xpe != null ? xpe.toString() : null);
			
			this.fail("XPath Parse Failed! Did not get syntax error as expected." + 
						"\n    expression:[" + expr + "]" +
						"\n    result:[" + (result == null ? "(null)" : result) + "]");
		} catch (XPathSyntaxException xse) {
			//success: syntax error as expected
		}
	}

	public void testParse (String expr, String expected) {
		if (expected != null) {
			testXPathValid(expr, expected);
		} else {
			testXPathInvalid(expr);
		}
	}
}


