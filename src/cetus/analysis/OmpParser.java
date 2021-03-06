package cetus.analysis;

import java.util.*;

/**
	* an OpenMP directive parser
	*/

public class OmpParser
{
	private static String [] token_array;
	private static int token_index;
	private	static HashMap omp_map;

	public OmpParser() {
	}

	private static String get_token()
	{
		return token_array[token_index++];
	}

	// consume one token
	private static void eat()
	{
		token_index++;
	}

	// match a token with the given string
	private static boolean match(String istr)
	{
		boolean answer = check(istr);
		if (answer == false) {
			System.out.println("OmpParser Syntax Error");
			display_tokens();
		}
		token_index++;
		return answer;
	}

	// match a token with the given string, but do not consume a token
	private static boolean check(String istr)
	{
		if ( end_of_token() ) 
			return false;
		return ( token_array[token_index].compareTo(istr) == 0 ) ? true : false;
	}	

	private static void display_tokens()
	{
		for (int i=0; i<token_array.length; i++)
		{
			System.out.println("token_array[" + i + "] = " + token_array[i]);
		}
		System.out.println();
	}

	private static boolean end_of_token()
	{
		return (token_index >= token_array.length) ? true : false;
	}

	// parse_omp_pragma returns TRUE, if the omp pragma is attached to the next
	// statement. It returns false, if it is not attached to any statement.
	public static boolean parse_omp_pragma(HashMap input_map, String [] str_array)
	{
		omp_map = input_map;
		token_array = str_array;
		token_index = 3;		// "#", "pragma", "omp" have already been matched

		display_tokens();

		String construct = "omp_" + get_token();
		switch (omp_pragma.valueOf(construct)) {
			case omp_parallel 		: parse_omp_parallel(); return true;
			case omp_for 					:	parse_omp_for			(); return true;
			case omp_sections 		: parse_omp_sections(); return true;
			case omp_section 			:	parse_omp_section	(); return true;
			case omp_single 			:	parse_omp_single	(); return true;
			case omp_task 				: parse_omp_task    (); return true;
			case omp_master 			:	parse_omp_master	(); return true;
			case omp_critical 		: parse_omp_critical(); return true;
			case omp_barrier 			:	parse_omp_barrier	(); return false;
			case omp_taskwait 		:	parse_omp_taskwait(); return false;
			case omp_atomic 			:	parse_omp_atomic	(); return true;
			case omp_flush 				:	parse_omp_flush		(); return false;
			case omp_ordered 			:	parse_omp_ordered	(); return true;
			case omp_threadprivate:	parse_omp_threadprivate(); return false;
//		default : throw new NonOmpDirectiveException();
			default : OmpParserError("Not Supported Construct");
		}
		return true;		// meaningless return statement
	}

	/** ---------------------------------------------------------------
		*		2.4 parallel Construct
		*
		*		#pragma omp parallel [clause[[,] clause]...] new-line
		*			structured-block
		*
		*		where clause is one of the following
		*			if(scalar-expression)
		*			num_threads(integer-expression)
		*			default(shared|none)
		*			private(list)
		*			firstprivate(list)
		*			shared(list)
		*			copyin(list)
		*			reduction(operator:list)
	  * --------------------------------------------------------------- */

	private static void parse_omp_parallel()
	{
		omp_map.put("parallel", "true");

		if ( check("for") )
		{
			eat();
			parse_omp_parallel_for();
		}
		else if ( check("sections") )
		{
			eat();
			parse_omp_parallel_sections();
		}
		else 
		{
			while (end_of_token() == false) 
			{
				String clause = "token_" + get_token();
				System.out.println("clause=" + clause);
				switch (omp_clause.valueOf(clause)) {
					case token_if 					:	parse_omp_if(); break;
					case token_num_threads	:	parse_omp_num_threads(); break;
					case token_default 			:	parse_omp_default(); break;
					case token_private			:	parse_omp_private(); break;
					case token_firstprivate	:	parse_omp_firstprivate(); break;
					case token_shared 			:	parse_omp_shared(); break;
					case token_copyin 			:	parse_omp_copyin(); break;
					case token_reduction		:	parse_omp_reduction(); break;
					default : OmpParserError("NoSuchParallelConstruct : " + clause);
				}
			}
		}
	}

	/** ---------------------------------------------------------------
		*		2.5 Worksharing Constructs
		*		OpenMP defines the following worksharing constructs
		*		- loop, sections, single, workshare(FORTRAN only) construct
	  * --------------------------------------------------------------- */

	/** ---------------------------------------------------------------
		*		2.5.1 Loop Construct
		*
		*		#pragma omp for [clause[[,] clause]...] new-line
		*			for-loops
		*		where clause is one of the following
		*			private(list)
		*			firstprivate(list)
		*			lastprivate(list)
		*			reduction(operator:list)
		*			schedule(kind[, chunk_size])
		*			collapse(n)
		*			ordered
		*			nowait
	  * --------------------------------------------------------------- */

	private static void parse_omp_for()
	{
		while (end_of_token() == false) 
		{
			String clause = "token_" + get_token();

			omp_map.put("for", "true");
			switch (omp_clause.valueOf(clause)) {
				case token_private			:	parse_omp_private(); break;
				case token_firstprivate	:	parse_omp_firstprivate(); break;
				case token_lastprivate	:	parse_omp_lastprivate(); break;
				case token_reduction		:	parse_omp_reduction(); break;
				case token_schedule			:	parse_omp_schedule(); break;
				case token_collapse			:	parse_omp_collapse(); break;
				case token_ordered			:	parse_omp_ordered(); break;
				case token_nowait				:	parse_omp_nowait(); break;
				default : OmpParserError("NoSuchParallelConstruct");
			}
		}
	}

	/** ---------------------------------------------------------------
		*		2.5.2 sections Construct
		*
		*		#pragma omp sections [clause[[,] clause]...] new-line
		*		{
		*			[#pragma omp section new-line]
		*				structured-block
		*			[#pragma omp section new-line
		*				structured-block]
		*		}
		*		where clause is one of the following
		*			private(list)
		*			firstprivate(list)
		*			lastprivate(list)
		*			reduction(operator:list)
		*			nowait
	  * --------------------------------------------------------------- */

	private static void parse_omp_sections()
	{
		while (end_of_token() == false) 
		{
			String clause = "token_" + get_token();

			omp_map.put("sections", "true");
			switch (omp_clause.valueOf(clause)) {
				case token_private			:	parse_omp_private(); break;
				case token_firstprivate	:	parse_omp_firstprivate(); break;
				case token_lastprivate	:	parse_omp_lastprivate(); break;
				case token_reduction		:	parse_omp_reduction(); break;
				case token_nowait				:	parse_omp_nowait(); break;
				default : OmpParserError("NoSuchParallelConstruct");
			}
		}
	}

	private static void parse_omp_section()
	{
		omp_map.put("section", "true");
	}

	/** ---------------------------------------------------------------
		*		2.5.3 single Construct
		*
		*		#pragma omp single [clause[[,] clause]...] new-line
		*				structured-block
		*
		*		where clause is one of the following
		*			private(list)
		*			firstprivate(list)
		*			copyprivate(list)
		*			nowait
	  * --------------------------------------------------------------- */

	private static void parse_omp_single()
	{
		while (end_of_token() == false) 
		{
			String clause = "token_" + get_token();

			omp_map.put("single", "true");
			switch (omp_clause.valueOf(clause)) {
				case token_private			:	parse_omp_private(); break;
				case token_firstprivate	:	parse_omp_firstprivate(); break;
				case token_copyprivate	:	parse_omp_copyprivate(); break;
				case token_nowait				:	parse_omp_nowait(); break;
				default : OmpParserError("NoSuchParallelConstruct");
			}
		}
	}

	/** ---------------------------------------------------------------
		*		2.6 Combined Parallel Worksharing Constructs
		*
		*		2.6.1 parallel loop Construct
		*
		*		#pragma omp parallel for [clause[[,] clause]...] new-line
		*				for-loop
		*		
		*		where clause can be any of the clauses accepted by the parallel
		*		or for directives, except the nowait clause, with identical
		*		meanings and restrictions
	  * --------------------------------------------------------------- */

	private static void parse_omp_parallel_for()
	{
		while (end_of_token() == false) 
		{
			String clause = "token_" + get_token();

			omp_map.put("for", "true");
			switch (omp_clause.valueOf(clause)) {
				case token_if 					:	parse_omp_if(); break;
				case token_num_threads	:	parse_omp_num_threads(); break;
				case token_default 			:	parse_omp_default(); break;
				case token_private			:	parse_omp_private(); break;
				case token_firstprivate	:	parse_omp_firstprivate(); break;
				case token_shared 			:	parse_omp_shared(); break;
				case token_copyin 			:	parse_omp_copyin(); break;
				case token_reduction		:	parse_omp_reduction(); break;
				case token_lastprivate	:	parse_omp_lastprivate(); break;
				case token_schedule			:	parse_omp_schedule(); break;
				case token_collapse			:	parse_omp_collapse(); break;
				case token_ordered			:	parse_omp_ordered(); break;
				default : OmpParserError("NoSuchParallelConstruct");
			}
		}
	}

	/** ---------------------------------------------------------------
		*		2.6.2 parallel sections Construct
		*
		*		#pragma omp sections [clause[[,] clause]...] new-line
		*		{
		*			[#pragma omp section new-line]
		*				structured-block
		*			[#pragma omp section new-line
		*				structured-block]
		*		}
		*		
		*		where clause can be any of the clauses accepted by the parallel
		*		or sections directives, except the nowait clause, with identical
		*		meanings and restrictions
	  * --------------------------------------------------------------- */

	private static void parse_omp_parallel_sections()
	{
		while (end_of_token() == false) 
		{
			String clause = "token_" + get_token();

			omp_map.put("sections", "true");
			switch (omp_clause.valueOf(clause)) {
				case token_if 					:	parse_omp_if(); break;
				case token_num_threads	:	parse_omp_num_threads(); break;
				case token_default 			:	parse_omp_default(); break;
				case token_private			:	parse_omp_private(); break;
				case token_firstprivate	:	parse_omp_firstprivate(); break;
				case token_shared 			:	parse_omp_shared(); break;
				case token_copyin 			:	parse_omp_copyin(); break;
				case token_lastprivate	:	parse_omp_lastprivate(); break;
				case token_reduction		:	parse_omp_reduction(); break;
				default : OmpParserError("NoSuchParallelConstruct");
			}
		}
	}

	/** ---------------------------------------------------------------
		*		2.7 task Construct
		*
		*		#pragma omp task [clause[[,] clause]...] new-line
		*			structured-block
		*
		*		where clause is one of the following
		*			if(scalar-expression)
		*			untied
		*			default(shared|none)
		*			private(list)
		*			firstprivate(list)
		*			shared(list)
	  * --------------------------------------------------------------- */

	private static void parse_omp_task()
	{
		while (end_of_token() == false) 
		{
			String clause = "token_" + get_token();
			omp_map.put("task", "true");
			switch (omp_clause.valueOf(clause)) {
				case token_if						:	parse_omp_if(); break;
				case token_untied				:	parse_omp_untied(); break;
				case token_default			:	parse_omp_default(); break;
				case token_private			:	parse_omp_private(); break;
				case token_firstprivate	:	parse_omp_firstprivate(); break;
				case token_shared				:	parse_omp_shared(); break;
				default : OmpParserError("NoSuchParallelConstruct");
			}
		}
	}

	/** ---------------------------------------------------------------
		*		2.8 Master and Synchronization Construct
		*
		*		-	master/critical/barrier/taskwait/atomic/flush/ordered
		*
		*		2.8.1 master Construct
		*
		*		#pragma omp master new-line
		*			structured-block
		*
	  * --------------------------------------------------------------- */

	private static void parse_omp_master()
	{
		omp_map.put("master", "true");
	}

	private static void parse_omp_critical()
	{
		String name = null;
		if (end_of_token() == false)
		{
			match("(");
			name = new String(get_token());
			match(")");
		}
		omp_map.put("critical", name);
	}

	private static void parse_omp_barrier()
	{
		omp_map.put("barrier", "true");
	}

	private static void parse_omp_taskwait()
	{
		omp_map.put("taskwait", "true");
	}

	private static void parse_omp_atomic()
	{
		omp_map.put("atomic", "true");
	}

	private static void parse_omp_flush()
	{
		HashSet<String> set = new HashSet<String>();
		match("(");
		parse_commaSeparatedList(set);
		match(")");
		omp_map.put("flush", set);
	}

	private static void parse_omp_ordered()
	{
		omp_map.put("ordered", "true");
	}

	/** ---------------------------------------------------------------
		*		2.9 Data Environment
		*
		*		2.9.1 read the specification
		*
		*		2.9.2 threadprivate Directive
		*
	  * --------------------------------------------------------------- */

	private static void parse_omp_threadprivate()
	{
		HashSet<String> set = new HashSet<String>();
		match("(");
		parse_commaSeparatedList(set);
		match(")");
		omp_map.put("threadprivate", set);
	}

	
	/** ---------------------------------------------------------------
		*
		*		A collection of parser routines for OpenMP clauses
		*
	  * --------------------------------------------------------------- */

	/**
		*	This function parses a list of strings between a parenthesis, for example, 
		* (scalar-expression) or (integer-expression).
		*/
	private static String parse_ParenEnclosedExpr()
	{
		String str = null;
		int paren_depth = 1;
		match("(");
		while (true) {
			if (check("(")) { paren_depth++; }
			if (check(")")) 
			{
				if (--paren_depth==0) break;
			}
			if (str==null)
			{
				str = new String(get_token());
			}
			else
			{
				str.concat((" " + get_token()));
			}
		}
		match(")");
		return str;
	}

	// it is assumed that a (scalar-expression) is of the form (size < N)
	private static void parse_omp_if()
	{
		String str = parse_ParenEnclosedExpr();
		omp_map.put("if", str);
	}

	// it is assumed that a (integer-expression) is of the form (4)
	private static void parse_omp_num_threads()
	{
		String str = parse_ParenEnclosedExpr();
		omp_map.put("num_threads", str);	
	}

	/**
		* schedule(kind[, chunk_size])
		*/
	private static void parse_omp_schedule()
	{
		String str = null;

		match("(");
		// schedule(static, chunk_size), schedule(dynamic, chunk_size), schedule(guided, chunk_size)
		if ( check("static") || check("dynamic") || check("guided") )
		{ 
			str = new String(get_token());
			if (check(","))
			{
				match(",");
				eat();		// consume "chunk_size"	
			}
		}
		// schedule(auto), schedule(runtime)
		else if ( check("auto") || check("runtime") )
		{
			str = new String(get_token());
		}
		else {
			OmpParserError("No such scheduling kind");
		}
		match(")");
		omp_map.put("schedule", str);
	}

	private static void parse_omp_collapse() 
	{
		match("(");
		String int_str = new String(get_token());
		match(")");
		omp_map.put("collapse", int_str);	
	}

	private static void parse_omp_nowait() { omp_map.put("nowait", "true"); }

	private static void parse_omp_untied() { omp_map.put("untied", "true"); }

	private static void parse_omp_default()
	{
		match("(");
		if ( check("shared") || check("none") )
		{
			omp_map.put("default", new String(get_token()));
		}	
		else {
			OmpParserError("NoSuchParallelDefaultCluase");
		}
		match(")");
	}

	private static void parse_omp_private()
	{
		HashSet<String> set = new HashSet<String>();
		match("(");
		parse_commaSeparatedList(set);
		match(")");
		omp_map.put("private", set);
	}

	private static void parse_omp_firstprivate()
	{
		HashSet<String> set = new HashSet<String>();
		match("(");
		parse_commaSeparatedList(set);
		match(")");
		omp_map.put("firstprivate", set);
	}

	private static void parse_omp_lastprivate()
	{
		HashSet<String> set = new HashSet<String>();
		match("(");
		parse_commaSeparatedList(set);
		match(")");
		omp_map.put("lastprivate", set);
	}

	private static void parse_omp_copyprivate()
	{
		HashSet<String> set = new HashSet<String>();
		match("(");
		parse_commaSeparatedList(set);
		match(")");
		omp_map.put("copyprivate", set);
	}

	private static void parse_omp_shared()
	{
		HashSet<String> set = new HashSet<String>();
		match("(");
		parse_commaSeparatedList(set);
		match(")");
		omp_map.put("shared", set);
	}

	private static void parse_omp_copyin()
	{
		HashSet<String> set = new HashSet<String>();
		match("(");
		parse_commaSeparatedList(set);
		match(")");
		omp_map.put("copyin", set);
	}

	// reduction(oprator:list)
	private static void parse_omp_reduction()
	{
		HashMap reduction_map = null;
		HashSet<String> set = null;
		String op = null;

		match("(");

		// Discover the kind of reduction operator (+, etc)
		if (check("+") || check("*") || check("-") || check("&") || check("|") || check("^") || 
				check("&&") || check("||") )
		{
			op = get_token();
			System.out.println("reduction op:" + op);
		}
		else {
			OmpParserError("Undefined reduction operator");
		}

		// check if there is already a reduction annotation with the same operator in the set
		for (String ikey : (Set<String>)(omp_map.keySet()))
		{
			if (ikey.compareTo("reduction") == 0)
			{
				reduction_map = (HashMap)(omp_map.get(ikey));
				set = (HashSet<String>)(reduction_map.get(op));
			}
		}
		if (reduction_map == null) { reduction_map = new HashMap(4); } 
		if (set == null) { set = new HashSet<String>(); }

		if ( match(":")==false ) 
		{
			OmpParserError("colon expected before a list of reduction variables");
		}

		parse_commaSeparatedList(set);
		match(")");

		reduction_map.put(op, set);
		omp_map.put("reduction", reduction_map);

	}

	/**
		*	This function reads a list of comma-separated variables
		* It checks the right parenthesis to end the parsing, but does not consume it.
		*/
	private static void parse_commaSeparatedList(HashSet<String> set)
	{
		for (;;) {
			set.add( get_token() );
			if ( check(")") )
			{
				break;
			}
			else if ( match(",") == false )
			{
				OmpParserError("comma expected in comma separated list");
			}
		}
	}

	private static void notSupportedWarning(String text)
	{
		System.out.println("Not Supported OpenMP feature: " + text); 
	}

	private static void OmpParserError(String text)
	{
		System.out.println("OpenMP Parser Syntax Error: " + text);
		display_tokens();
	}

	public static enum omp_pragma
	{
		omp_parallel, 
		omp_for, 
		omp_sections, 
		omp_section, 
		omp_single, 
		omp_task, 
		omp_master, 
		omp_critical, 
		omp_barrier, 
		omp_taskwait,
		omp_atomic, 
		omp_flush, 
		omp_ordered,
		omp_threadprivate
	}

	public static enum omp_clause
	{
		token_if,
		token_num_threads,
		token_default,
		token_private,
		token_firstprivate,
		token_lastprivate,
		token_shared,
		token_copyprivate,
		token_copyin,
		token_schedule,
		token_nowait,
		token_ordered,
		token_untied,
		token_collapse,
		token_reduction
	}

}
