// $ANTLR 2.7.5 (20050128): "NewCParser.g" -> "NewCParser.java"$

package cetus.base.grammars;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

import java.io.*;

import antlr.CommonAST;
import antlr.DumpASTVisitor;
import java.util.*;
import cetus.hir.*;
//John A. Stratton: September 2008
// Sometimes need to access the parameters to slightly change parser behavior.
import cetus.exec.*;

public class NewCParser extends antlr.LLkParser       implements NEWCTokenTypes
 {



		Expression baseEnum = null,curEnum = null;
		NewCLexer curLexer=null;
		boolean isFuncDef = false;
		boolean isExtern = false;
	   	PreprocessorInfoChannel preprocessorInfoChannel = null;
        SymbolTable symtab = null;
        CompoundStatement curr_cstmt = null;
        boolean hastypedef = false;
        Hashtable typetable = null;
        ArrayList currproc = new ArrayList();
        Declaration prev_decl = null;
        boolean old_style_func = false;
        Hashtable func_decl_list = new Hashtable();
        public void getPreprocessorInfoChannel(PreprocessorInfoChannel preprocChannel )
		{
        	preprocessorInfoChannel = preprocChannel;

		}
		public void setLexer(NewCLexer lexer){
			curLexer=lexer;
			curLexer.setParser(this);
		}
		public NewCLexer getLexer(){
			return curLexer;
		}
		public Vector getPragma(int a){
		 	return preprocessorInfoChannel.extractLinesPrecedingTokenNumber
								(new Integer(a));
		}
        public void putPragma(Token sline,SymbolTable sym){
                	Vector v  = null;
                	v = getPragma(((CToken)sline).getTokenNumber());
        			Iterator iter = v.iterator();
        			Pragma p = null;
        				Annotation anote = null;
        			while(iter.hasNext()){
        				p = (Pragma)iter.next();
        			 	anote = new Annotation(p.str);
        				if(p.type ==Pragma.pragma)
        					anote.setPrintMethod(Annotation.print_raw_method);
        				else if(p.type ==Pragma.comment)
        					anote.setPrintMethod(Annotation.print_raw_method);
        				//sym.addStatement(new DeclarationStatement(anote));
        				if(sym instanceof CompoundStatement)
        					((CompoundStatement)sym).addStatement(new DeclarationStatement(anote));
        				else
        					sym.addDeclaration(anote);
        			}
        }

    // Suppport C++-style single-line comments?
    public static boolean CPPComments = true;
	public Stack symtabstack = new Stack();
	public Stack typestack = new Stack();
	public void enterSymtab(SymbolTable curr_symtab){
		symtabstack.push(symtab);
		typetable = new Hashtable();
		typestack.push(typetable);
		symtab = curr_symtab;
	}
	public void exitSymtab(){
		Object o = symtabstack.pop();
		if(o != null){
			typestack.pop();
			typetable = (Hashtable)(typestack.peek());
			symtab = (SymbolTable)o;
		}
	}
    public boolean isTypedefName(String name) {
		/*
		boolean returnValue = false;
		Object o = typetable.get(name);
		if(o == null){
			returnValue = false;
		}
		else{
			returnValue = true;
		}

      if(name.equals("__builtin_va_list"))
      	returnValue = true;


      return returnValue;
      */

      //System.err.println("Typename "+name);
      int n = typestack.size()-1;
      Object d = null;
      while(n>=0){
          	d = ((Hashtable)(typestack.get(n))).get(name);

      		if(d != null )
      			return true;


			n--;
      }
      if(name.equals("__builtin_va_list"))
      	return true;

      //System.err.println("Typename "+name+" not found");

      return false;

      /*
      System.err.println("Checking for typedef name: "+name);
      boolean returnValue = false;
      Declaration d = Tools.findSymbol(symtab,new Identifier(name));
      if(d != null && d instanceof VariableDeclaration){
      	VariableDeclaration v = (VariableDeclaration)d;
      	List l = v.getSpecifiers();
      	Iterator i = l.iterator();
      	Object o = null;
      	while(i.hasNext()){
      		o = i.next();
      		if(o == Specifier.TYPEDEF)
      			return true;
      	}

      }
      if(name.equals("__builtin_va_list"))
      	returnValue = true;

      if(returnValue ==false){

      	System.err.println(name+" is not a typedef name");
      	System.err.println(symtab.getClass());
      }
      return returnValue;
      */
    }





        int traceDepth = 0;
        public void reportError(RecognitionException ex){
          try {
            //System.err.println("ANTLR Parsing Error: "+ex + " token name:" + tokenNames[LA(1)]);
            //System.err.println("Before: ");
	    //((Printable)symtab).print(System.err);
	    //System.err.println();
	    System.err.println("ANTLR Parsing Error: "+"Exception Type: "+ex.getClass().getName());
            System.err.println("Source: "+getLexer().lineObject.getSource()+" Line:"+ex.getLine()+" Column: "+ex.getColumn() +" token name:" + tokenNames[LA(1)]);
            ex.printStackTrace(System.err);
            System.exit(1);
          }
	  catch (TokenStreamException e) {
            System.err.println("ANTLR Parsing Error: "+ex);
            ex.printStackTrace(System.err);
	    System.exit(1);
          }
        }
        public void reportError(String s) {
            System.err.println("ANTLR Parsing Error from String: " + s);
        }
        public void reportWarning(String s) {
            System.err.println("ANTLR Parsing Warning from String: " + s);
        }
        public void match(int t) throws MismatchedTokenException {
          boolean debugging = false;

          if ( debugging ) {
           for (int x=0; x<traceDepth; x++) System.out.print(" ");
           try {
            System.out.println("Match("+tokenNames[t]+") with LA(1)="+
                tokenNames[LA(1)] + ((inputState.guessing>0)?" [inputState.guessing "+ inputState.guessing + "]":""));
           }
           catch (TokenStreamException e) {
            System.out.println("Match("+tokenNames[t]+") " + ((inputState.guessing>0)?" [inputState.guessing "+ inputState.guessing + "]":""));

           }

          }
          try {
            if ( LA(1)!=t ) {
                if ( debugging ){
                    for (int x=0; x<traceDepth; x++) System.out.print(" ");
                    System.out.println("token mismatch: "+tokenNames[LA(1)]
                                + "!="+tokenNames[t]);
                }
	        throw new MismatchedTokenException(tokenNames, LT(1), t, false, getFilename());

            } else {
                // mark token as consumed -- fetch next token deferred until LA/LT
                consume();
            }
          }
          catch (TokenStreamException e) {
          }

        }
        public void traceIn(String rname) {
          traceDepth += 1;
          for (int x=0; x<traceDepth; x++) System.out.print(" ");
          try {
            System.out.println("> "+rname+"; LA(1)==("+ tokenNames[LT(1).getType()]
                + ") " + LT(1).getText() + " [inputState.guessing "+ inputState.guessing + "]");
          }
          catch (TokenStreamException e) {
          }
        }
        public void traceOut(String rname) {
          for (int x=0; x<traceDepth; x++) System.out.print(" ");
          try {
            System.out.println("< "+rname+"; LA(1)==("+ tokenNames[LT(1).getType()]
                + ") "+LT(1).getText() + " [inputState.guessing "+ inputState.guessing + "]");
          }
          catch (TokenStreamException e) {
          }
          traceDepth -= 1;
        }


protected NewCParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public NewCParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected NewCParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public NewCParser(TokenStream lexer) {
  this(lexer,2);
}

public NewCParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
}

	public final TranslationUnit  translationUnit(
		TranslationUnit init_tunit
	) throws RecognitionException, TokenStreamException {
		TranslationUnit tunit;
		
		
					/* build a new Translation Unit */
					if(init_tunit == null)
						tunit = new TranslationUnit(getLexer().originalSource);
					else
						tunit = init_tunit;
					enterSymtab(tunit);
					/*
					symtab = tunit;
					typetable = new Hashtable();
					*/
		
				
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PREPROC_DIRECTIVE:
			case LITERAL_typedef:
			case SEMI:
			case LITERAL_asm:
			case LITERAL_volatile:
			case LITERAL_struct:
			case LITERAL_union:
			case LITERAL_enum:
			case LITERAL_auto:
			case LITERAL_register:
			case LITERAL_extern:
			case LITERAL_static:
			case LITERAL_inline:
			case LITERAL___global__:
			case LITERAL___device__:
			case LITERAL___host__:
			case LITERAL___kernel:
			case LITERAL___global:
			case LITERAL___local:
			case LITERAL_const:
			case LITERAL___shared__:
			case LITERAL___constant__:
			case LITERAL_void:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_long:
			case LITERAL_float:
			case LITERAL_double:
			case LITERAL_signed:
			case LITERAL_unsigned:
			case LITERAL__Bool:
			case LITERAL__Complex:
			case LITERAL__Imaginary:
			case LITERAL_bool:
			case LITERAL_typeof:
			case LPAREN:
			case LITERAL___complex:
			case ID:
			case LITERAL___attribute:
			case STAR:
			{
				externalList(tunit);
				break;
			}
			case EOF:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				//System.err.println(typetable);
								exitSymtab();
							
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		return tunit;
	}
	
	public final void externalList(
		TranslationUnit tunit
	) throws RecognitionException, TokenStreamException {
		
		Token  pre_dir = null;
		boolean flag = true;	
		
		try {      // for error handling
			{
			int _cnt5=0;
			_loop5:
			do {
				switch ( LA(1)) {
				case PREPROC_DIRECTIVE:
				{
					pre_dir = LT(1);
					match(PREPROC_DIRECTIVE);
					if ( inputState.guessing==0 ) {
						
												String value = (pre_dir.getText()).substring(7).trim();
												putPragma(pre_dir,symtab);
												/*
												if(value.startsWith("endinclude")){
						
														flag = true;
												}
												else if(value.startsWith("startinclude")){
						
														flag = false;
												}
												*/
												Annotation anote = new Annotation(pre_dir.getText());
												tunit.addDeclaration(anote);
												anote.setPrintMethod(Annotation.print_raw_method);
						
						
						
												//elist.add(pre_dir.getText());
											
					}
					break;
				}
				case LITERAL_typedef:
				case SEMI:
				case LITERAL_asm:
				case LITERAL_volatile:
				case LITERAL_struct:
				case LITERAL_union:
				case LITERAL_enum:
				case LITERAL_auto:
				case LITERAL_register:
				case LITERAL_extern:
				case LITERAL_static:
				case LITERAL_inline:
				case LITERAL___global__:
				case LITERAL___device__:
				case LITERAL___host__:
				case LITERAL___kernel:
				case LITERAL___global:
				case LITERAL___local:
				case LITERAL_const:
				case LITERAL___shared__:
				case LITERAL___constant__:
				case LITERAL_void:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_long:
				case LITERAL_float:
				case LITERAL_double:
				case LITERAL_signed:
				case LITERAL_unsigned:
				case LITERAL__Bool:
				case LITERAL__Complex:
				case LITERAL__Imaginary:
				case LITERAL_bool:
				case LITERAL_typeof:
				case LPAREN:
				case LITERAL___complex:
				case ID:
				case LITERAL___attribute:
				case STAR:
				{
					externalDef(tunit);
					break;
				}
				default:
				{
					if ( _cnt5>=1 ) { break _loop5; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				}
				_cnt5++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void externalDef(
		TranslationUnit tunit
	) throws RecognitionException, TokenStreamException {
		
		
					Declaration decl = null;
				
		
		try {      // for error handling
			boolean synPredMatched8 = false;
			if (((_tokenSet_1.member(LA(1))) && (_tokenSet_2.member(LA(2))))) {
				int _m8 = mark();
				synPredMatched8 = true;
				inputState.guessing++;
				try {
					{
					if ((LA(1)==LITERAL_typedef) && (true)) {
						match(LITERAL_typedef);
					}
					else if ((_tokenSet_1.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
						declaration();
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
				catch (RecognitionException pe) {
					synPredMatched8 = false;
				}
				rewind(_m8);
				inputState.guessing--;
			}
			if ( synPredMatched8 ) {
				decl=declaration();
				if ( inputState.guessing==0 ) {
					
								if(decl != null){
									/*System.err.println("Adding Declaration");
									decl.print(System.err);
									System.err.println("");*/
									//Tools.printStatus("Adding Declaration: ",3);
									//Tools.printlnStatus(decl,3);
									tunit.addDeclaration(decl);
								}
					
							
				}
			}
			else {
				boolean synPredMatched10 = false;
				if (((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))))) {
					int _m10 = mark();
					synPredMatched10 = true;
					inputState.guessing++;
					try {
						{
						functionPrefix();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched10 = false;
					}
					rewind(_m10);
					inputState.guessing--;
				}
				if ( synPredMatched10 ) {
					decl=functionDef();
					if ( inputState.guessing==0 ) {
						
									/*System.err.println("Adding Declaration");
										decl.print(System.err);
										System.err.println("");*/
									//Tools.printStatus("Adding Declaration: ",3);
									//	Tools.printlnStatus(decl,3);
									tunit.addDeclaration(decl);
					}
				}
				else if ((_tokenSet_5.member(LA(1))) && (_tokenSet_6.member(LA(2)))) {
					decl=typelessDeclaration();
					if ( inputState.guessing==0 ) {
						/*System.err.println("Adding Declaration");
										decl.print(System.err);
										System.err.println("");*/
									//	Tools.printStatus("Adding Declaration: ",3);
									//	Tools.printlnStatus(decl,3);
									tunit.addDeclaration(decl);
					}
				}
				else if ((LA(1)==LITERAL_asm) && (LA(2)==LCURLY||LA(2)==LITERAL_volatile)) {
					asm_expr();
				}
				else if ((LA(1)==SEMI)) {
					match(SEMI);
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_7);
				} else {
				  throw ex;
				}
			}
		}
		
	public final Declaration  declaration() throws RecognitionException, TokenStreamException {
		Declaration bdecl;
		
		Token  dsemi = null;
		bdecl=null;
						List dspec=null;
						List idlist=null;
						
		
		try {      // for error handling
			dspec=declSpecifiers();
			{
			switch ( LA(1)) {
			case LITERAL_asm:
			case LPAREN:
			case ID:
			case LITERAL___attribute:
			case STAR:
			{
				idlist=initDeclList();
				break;
			}
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
					if(idlist != null){
				
				
						if(old_style_func){
				
							Iterator iter = idlist.iterator();
							Declarator d  = null;
							Declaration newdecl = null;
							while(iter.hasNext()){
								d = (Declarator)(iter.next());
								newdecl = new VariableDeclaration(dspec,d);
								func_decl_list.put(d.getSymbol().toString(),newdecl);
				
							}
							bdecl = null;
										/*
											Iterator iter = idlist.iterator();
							Declarator d  = null;
							Declaration newdecl = null;
							while(iter.hasNext()){
								d = (Declarator)(iter.next());
								newdecl = new VariableDeclaration(dspec,d);
								symtab.addDeclaration(newdecl);
				
							}
							bdecl = null;
						*/
										}
						else
							bdecl = new VariableDeclaration(dspec,idlist);
						prev_decl = null;
					}
					else{
						/*
						 * Looks like a forward declaration
						 */
				
						if(prev_decl != null){
				
							bdecl = prev_decl;
							prev_decl = null;
						}
					}
				
					hastypedef = false;
				
			}
			{
			int _cnt28=0;
			_loop28:
			do {
				if ((LA(1)==SEMI) && (_tokenSet_8.member(LA(2)))) {
					dsemi = LT(1);
					match(SEMI);
				}
				else {
					if ( _cnt28>=1 ) { break _loop28; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt28++;
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
				
					int sline = 0;
					sline = dsemi.getLine();
				
							putPragma(dsemi,symtab);
							hastypedef = false;
				
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_8);
			} else {
			  throw ex;
			}
		}
		return bdecl;
	}
	
	public final void functionPrefix() throws RecognitionException, TokenStreamException {
		
		Declarator decl = null;
		
		try {      // for error handling
			{
			boolean synPredMatched14 = false;
			if (((_tokenSet_9.member(LA(1))) && (_tokenSet_10.member(LA(2))))) {
				int _m14 = mark();
				synPredMatched14 = true;
				inputState.guessing++;
				try {
					{
					functionDeclSpecifiers();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched14 = false;
				}
				rewind(_m14);
				inputState.guessing--;
			}
			if ( synPredMatched14 ) {
				functionDeclSpecifiers();
			}
			else if ((_tokenSet_5.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			decl=declarator();
			{
			_loop16:
			do {
				if ((_tokenSet_1.member(LA(1)))) {
					declaration();
				}
				else {
					break _loop16;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case VARARGS:
			{
				match(VARARGS);
				break;
			}
			case SEMI:
			case LCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop19:
			do {
				if ((LA(1)==SEMI)) {
					match(SEMI);
				}
				else {
					break _loop19;
				}
				
			} while (true);
			}
			match(LCURLY);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	public final Procedure  functionDef() throws RecognitionException, TokenStreamException {
		Procedure curFunc;
		
		
						CompoundStatement stmt=null;
						Declaration decl=null;
						Declarator bdecl=null;
						List dspec=null;
						curFunc = null;
						String declName=null;
						int dcount = 0;
						SymbolTable prev_symtab =null;
						SymbolTable temp_symtab = new CompoundStatement();
					
		
		try {      // for error handling
			{
			boolean synPredMatched157 = false;
			if (((_tokenSet_9.member(LA(1))) && (_tokenSet_10.member(LA(2))))) {
				int _m157 = mark();
				synPredMatched157 = true;
				inputState.guessing++;
				try {
					{
					functionDeclSpecifiers();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched157 = false;
				}
				rewind(_m157);
				inputState.guessing--;
			}
			if ( synPredMatched157 ) {
				if ( inputState.guessing==0 ) {
					isFuncDef = true;
				}
				dspec=functionDeclSpecifiers();
			}
			else if ((_tokenSet_5.member(LA(1))) && (_tokenSet_4.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
					if(dspec == null)
						dspec = new ArrayList();
				
			}
			bdecl=declarator();
			if ( inputState.guessing==0 ) {
				
					/*
						prev_symtab = symtab;
						symtab = temp_symtab;
					*/
					enterSymtab(temp_symtab);
						old_style_func = true;
						func_decl_list.clear();
				
			}
			{
			_loop159:
			do {
				if ((_tokenSet_1.member(LA(1)))) {
					declaration();
					if ( inputState.guessing==0 ) {
						dcount++;
					}
				}
				else {
					break _loop159;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case VARARGS:
			{
				match(VARARGS);
				break;
			}
			case SEMI:
			case LCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop162:
			do {
				if ((LA(1)==SEMI)) {
					match(SEMI);
				}
				else {
					break _loop162;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
					old_style_func = false;
					/*
					symtab = prev_symtab;
					*/
					exitSymtab();
				
			}
			if ( inputState.guessing==0 ) {
				
				
					isFuncDef = false;
				
					if(dcount > 0){
						HashMap hm = null;
						Identifier name = null;
									    Declaration tdecl = null;
				
						/**
						 *  This implementation is not so good since it relies on
						 * the fact that function parameter starts from the second
						 *  children and getChildren returns a reference to the
						 * actual internal list
						 */
						ListIterator i = ((List)(bdecl.getChildren())).listIterator();
									    i.next();
									    while (i.hasNext())
									    {
									      VariableDeclaration vdec = (VariableDeclaration)i.next();
				
									      Iterator j = vdec.getDeclaredSymbols().iterator();
									      while (j.hasNext())
									      {
									      	// declarator name
									        name = (Identifier)(j.next());
									       /*
									        // find matching Declaration
				
									        hm = temp_symtab.getTable();
				
									        tdecl = (Declaration)(hm.get(name));
									        if(tdecl == null){
									        	//ByteArrayOutputStream bs = new ByteArrayOutputStream();
									        	//name.print(bs);
									        	System.err.println("cannot find symbol "+name+"------------"+hm);
									        }
				
									        // replace declaration
									        else
									        i.set(tdecl);
									        */
				
									        // find matching Declaration
				
									        tdecl = (Declaration)(func_decl_list.get(name.toString()));
									        if(tdecl == null){
									        	//ByteArrayOutputStream bs = new ByteArrayOutputStream();
									        	//name.print(bs);
									        	Tools.printlnStatus("cannot find symbol "+name+ "in old style function declaration, now assuming an int",1);
									        	tdecl = new VariableDeclaration(Specifier.INT,new VariableDeclarator((Identifier)name.clone())) ;
									        	i.set(tdecl);
									        }
									        // replace declaration
									        else
									        i.set(tdecl);
				
									      }
									    }
				
						Iterator diter = temp_symtab.getTable().entrySet().iterator();
					                	Object tobject = null;
					                	while(diter.hasNext()){
					                		tobject = diter.next();
					                		if(tobject instanceof Annotation)
					                			symtab.addDeclaration((Declaration)tobject);
					                	}
					                }
				
				
								
			}
			stmt=compoundStatement();
			if ( inputState.guessing==0 ) {
				
									if(stmt != null){
										curFunc = new Procedure(dspec,bdecl, stmt);
									}
									// function delaration
									else{
										curFunc = new Procedure(dspec,bdecl, stmt);
									}
									Tools.printStatus("Creating Procedure: ",1);
									Tools.printlnStatus(bdecl,1);
									// already handled in constructor
				
									//Iterator iter = currproc.iterator();
									//while(iter.hasNext()){
									//	curFunc.addDeclaration((Declaration)(iter.next()));
									//}
				
									//System.err.println(currproc);
									currproc.clear();
								
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_7);
			} else {
			  throw ex;
			}
		}
		return curFunc;
	}
	
	public final Declaration  typelessDeclaration() throws RecognitionException, TokenStreamException {
		Declaration decl;
		
		decl=null;
						List idlist=null;
		
		try {      // for error handling
			idlist=initDeclList();
			match(SEMI);
			if ( inputState.guessing==0 ) {
				decl = new VariableDeclaration(new ArrayList(),idlist);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_7);
			} else {
			  throw ex;
			}
		}
		return decl;
	}
	
	public final void asm_expr() throws RecognitionException, TokenStreamException {
		
		Expression expr1 = null;
		
		try {      // for error handling
			match(LITERAL_asm);
			{
			switch ( LA(1)) {
			case LITERAL_volatile:
			{
				match(LITERAL_volatile);
				break;
			}
			case LCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LCURLY);
			expr1=expr();
			match(RCURLY);
			{
			int _cnt24=0;
			_loop24:
			do {
				if ((LA(1)==SEMI) && (_tokenSet_7.member(LA(2)))) {
					match(SEMI);
				}
				else {
					if ( _cnt24>=1 ) { break _loop24; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt24++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_7);
			} else {
			  throw ex;
			}
		}
	}
	
	public final List  functionDeclSpecifiers() throws RecognitionException, TokenStreamException {
		List dspec;
		
		
			dspec = new ArrayList();
			Specifier type=null;
			Specifier tqual=null;
			Specifier tspec=null;
		
		
		try {      // for error handling
			{
			int _cnt167=0;
			_loop167:
			do {
				if (((LA(1) >= LITERAL_extern && LA(1) <= LITERAL___local)) && (_tokenSet_3.member(LA(2)))) {
					type=functionStorageClassSpecifier();
					if ( inputState.guessing==0 ) {
						dspec.add(type);
					}
				}
				else if ((_tokenSet_11.member(LA(1))) && (_tokenSet_3.member(LA(2)))) {
					tqual=typeQualifier();
					if ( inputState.guessing==0 ) {
						dspec.add(tqual);
					}
				}
				else {
					boolean synPredMatched166 = false;
					if (((_tokenSet_12.member(LA(1))) && (_tokenSet_10.member(LA(2))))) {
						int _m166 = mark();
						synPredMatched166 = true;
						inputState.guessing++;
						try {
							{
							if ((LA(1)==LITERAL_struct) && (true)) {
								match(LITERAL_struct);
							}
							else if ((LA(1)==LITERAL_union) && (true)) {
								match(LITERAL_union);
							}
							else if ((LA(1)==LITERAL_enum) && (true)) {
								match(LITERAL_enum);
							}
							else if ((_tokenSet_12.member(LA(1))) && (true)) {
								tspec=typeSpecifier();
							}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							
							}
						}
						catch (RecognitionException pe) {
							synPredMatched166 = false;
						}
						rewind(_m166);
						inputState.guessing--;
					}
					if ( synPredMatched166 ) {
						tspec=typeSpecifier();
						if ( inputState.guessing==0 ) {
							dspec.add(tspec);
						}
					}
					else {
						if ( _cnt167>=1 ) { break _loop167; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					}
					_cnt167++;
				} while (true);
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_5);
				} else {
				  throw ex;
				}
			}
			return dspec;
		}
		
	public final Declarator  declarator() throws RecognitionException, TokenStreamException {
		Declarator decl;
		
		Token  id = null;
		
						Expression expr1=null;
						String declName = null;
						decl = null;
						Declarator tdecl = null;
						IDExpression idex = null;
						List plist = null;
						List bp = null;
						Specifier aspec = null;
						boolean isArraySpec = false;
						boolean isNested = false;
						List llist = new ArrayList(1);
						List tlist = null;
					
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case STAR:
			{
				bp=pointerGroup();
				break;
			}
			case LITERAL_asm:
			case LPAREN:
			case ID:
			case LITERAL___attribute:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				/*
					if(bp == null)
						bp = new ArrayList();
				*/
				
			}
			{
			switch ( LA(1)) {
			case LITERAL_asm:
			case LITERAL___attribute:
			{
				attributeDecl();
				break;
			}
			case LPAREN:
			case ID:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case ID:
			{
				id = LT(1);
				match(ID);
				if ( inputState.guessing==0 ) {
					
							declName = id.getText();
							idex = new Identifier(declName);
											if(hastypedef){
												typetable.put(declName,"typedef");
											}
						
				}
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				tdecl=declarator();
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					
						               	
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop137:
			do {
				switch ( LA(1)) {
				case LPAREN:
				{
					plist=declaratorParamaterList();
					if ( inputState.guessing==0 ) {
						
								//decl = new Declarator(bp,idex,plist,null,null);
							
					}
					break;
				}
				case LBRACKET:
				{
					match(LBRACKET);
					{
					switch ( LA(1)) {
					case LITERAL_asm:
					case LPAREN:
					case ID:
					case STAR:
					case BAND:
					case PLUS:
					case MINUS:
					case INC:
					case DEC:
					case LITERAL_sizeof:
					case LITERAL___alignof:
					case BNOT:
					case LNOT:
					case LITERAL___real:
					case LITERAL___imag:
					case Number:
					case CharLiteral:
					case StringLiteral:
					{
						expr1=expr();
						break;
					}
					case RBRACKET:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(RBRACKET);
					if ( inputState.guessing==0 ) {
						
								isArraySpec = true;
						
									llist.add(expr1);
						
							
					}
					break;
				}
				default:
				{
					break _loop137;
				}
				}
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
									/*
									 * Possible combinations
									 * []+ ,  ()
									 */
									 if(plist != null){
				
						/*
						if(isArraySpec){
				
							aspec = new ArraySpecifier(llist);
						//aspec = new ArraySpecifier();
						tlist = new ArrayList();
						tlist.add(aspec);
						}
						*/
						/*
						 * ()
						 */
				
									}
									else{
										/*
										 * []+
										 */
				
				
										if(isArraySpec){
											/*
											 * []+
											 */
											aspec = new ArraySpecifier(llist);
											//aspec = new ArraySpecifier();
											tlist = new ArrayList();
											tlist.add(aspec);
										}
				
									}
									/*
								if(idex != null){
									decl = new Declarator(bp, idex, plist, tlist,null) ;
								}
								else{
									// this is wrong
									decl = new Declarator(bp, tdecl, plist) ;
								}
									*/
									if(bp == null)
								bp = new LinkedList();
									if(tdecl != null) // assume tlist == null
									{
										//assert tlist == null : "Assertion (tlist == null) failed 2";
										if(tlist == null)
									tlist = new LinkedList();
										//if(plist != null){
											decl = new NestedDeclarator(bp,tdecl,plist,tlist);
										//}
										//else
										//	decl = new NestedDeclarator(bp,tdecl,new LinkedList(),tlist);
									}
									else{
										if(plist != null) // assume tlist == null
									decl = new ProcedureDeclarator(bp,idex,plist);
							else{
								if(tlist != null)
									decl = new VariableDeclarator(bp,idex,tlist);
								else
									decl = new VariableDeclarator(bp,idex);
							}
									}
									//decl = new Declarator(bp, idex,tdecl,plist, tlist);
									//hastypedef = false;
				
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
		return decl;
	}
	
	public final List  initDeclList() throws RecognitionException, TokenStreamException {
		List dlist;
		
		
			        		Declarator decl=null;
							dlist = new ArrayList();
				
		
		try {      // for error handling
			decl=initDecl();
			if ( inputState.guessing==0 ) {
				
					        		dlist.add(decl);
					    		
			}
			{
			_loop96:
			do {
				if ((LA(1)==COMMA) && (_tokenSet_5.member(LA(2)))) {
					match(COMMA);
					decl=initDecl();
					if ( inputState.guessing==0 ) {
						
							        		dlist.add(decl);
							    		
					}
				}
				else {
					break _loop96;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_14);
			} else {
			  throw ex;
			}
		}
		return dlist;
	}
	
	public final Expression  expr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		Token  c = null;
		ret_expr = null; Expression expr1=null,expr2=null; List elist = new ArrayList();
			
		
		try {      // for error handling
			ret_expr=assignExpr();
			if ( inputState.guessing==0 ) {
				
					elist.add(ret_expr);
				
			}
			{
			_loop215:
			do {
				if ((LA(1)==COMMA) && (_tokenSet_15.member(LA(2)))) {
					c = LT(1);
					match(COMMA);
					expr1=assignExpr();
					if ( inputState.guessing==0 ) {
						elist.add(expr1);
						
					}
				}
				else {
					break _loop215;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
				
					if(elist.size() > 1){
						//System.err.println("CommaExpr");
						ret_expr = new CommaExpression(elist);
					}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_16);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final List  declSpecifiers() throws RecognitionException, TokenStreamException {
		List decls;
		
		decls = new ArrayList();
			Specifier spec = null;
			Specifier temp=null;
		
		try {      // for error handling
			{
			int _cnt33=0;
			_loop33:
			do {
				if ((_tokenSet_17.member(LA(1))) && (_tokenSet_18.member(LA(2)))) {
					spec=storageClassSpecifier();
					if ( inputState.guessing==0 ) {
						decls.add(spec);
					}
				}
				else if ((_tokenSet_11.member(LA(1))) && (_tokenSet_18.member(LA(2)))) {
					spec=typeQualifier();
					if ( inputState.guessing==0 ) {
						decls.add(spec);
					}
				}
				else {
					boolean synPredMatched32 = false;
					if (((_tokenSet_12.member(LA(1))) && (_tokenSet_19.member(LA(2))))) {
						int _m32 = mark();
						synPredMatched32 = true;
						inputState.guessing++;
						try {
							{
							if ((LA(1)==LITERAL_struct) && (true)) {
								match(LITERAL_struct);
							}
							else if ((LA(1)==LITERAL_union) && (true)) {
								match(LITERAL_union);
							}
							else if ((LA(1)==LITERAL_enum) && (true)) {
								match(LITERAL_enum);
							}
							else if ((_tokenSet_12.member(LA(1))) && (true)) {
								typeSpecifier();
							}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							
							}
						}
						catch (RecognitionException pe) {
							synPredMatched32 = false;
						}
						rewind(_m32);
						inputState.guessing--;
					}
					if ( synPredMatched32 ) {
						temp=typeSpecifier();
						if ( inputState.guessing==0 ) {
							
								decls.add(temp) ;
							
						}
					}
					else if ((LA(1)==LITERAL_asm||LA(1)==LITERAL___attribute) && (LA(2)==LPAREN)) {
						attributeDecl();
					}
					else {
						if ( _cnt33>=1 ) { break _loop33; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					}
					_cnt33++;
				} while (true);
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_20);
				} else {
				  throw ex;
				}
			}
			return decls;
		}
		
/*********************************
 * Specifier                      *
 **********************************/
	public final Specifier  storageClassSpecifier() throws RecognitionException, TokenStreamException {
		Specifier cspec;
		
		cspec= null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_auto:
			{
				match(LITERAL_auto);
				if ( inputState.guessing==0 ) {
					cspec = Specifier.AUTO;
				}
				break;
			}
			case LITERAL_register:
			{
				match(LITERAL_register);
				if ( inputState.guessing==0 ) {
					cspec = Specifier.REGISTER;
				}
				break;
			}
			case LITERAL_typedef:
			{
				match(LITERAL_typedef);
				if ( inputState.guessing==0 ) {
					
								cspec = Specifier.TYPEDEF;
								hastypedef = true;
							
				}
				break;
			}
			case LITERAL_extern:
			case LITERAL_static:
			case LITERAL_inline:
			case LITERAL___global__:
			case LITERAL___device__:
			case LITERAL___host__:
			case LITERAL___kernel:
			case LITERAL___global:
			case LITERAL___local:
			{
				cspec=functionStorageClassSpecifier();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_18);
			} else {
			  throw ex;
			}
		}
		return cspec;
	}
	
	public final Specifier  typeQualifier() throws RecognitionException, TokenStreamException {
		Specifier tqual;
		
		tqual=null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_const:
			{
				match(LITERAL_const);
				if ( inputState.guessing==0 ) {
					tqual = Specifier.CONST;
				}
				break;
			}
			case LITERAL_volatile:
			{
				match(LITERAL_volatile);
				if ( inputState.guessing==0 ) {
					tqual = Specifier.VOLATILE;
				}
				break;
			}
			case LITERAL___shared__:
			{
				match(LITERAL___shared__);
				if ( inputState.guessing==0 ) {
					tqual = Specifier.SHARED;
				}
				break;
			}
			case LITERAL___constant__:
			{
				match(LITERAL___constant__);
				if ( inputState.guessing==0 ) {
					tqual = Specifier.CONSTANT;
				}
				break;
			}
			case LITERAL___device__:
			{
				match(LITERAL___device__);
				if ( inputState.guessing==0 ) {
					tqual = Specifier.DEVICE;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
		return tqual;
	}
	
/***************************************************
 * Should a basic type be an int value or a class ? *
 ****************************************************/
	public final Specifier  typeSpecifier() throws RecognitionException, TokenStreamException {
		Specifier types;
		
		Token  i = null;
		types = null;
						String tname = null;
						Expression expr1 = null;
						List tyname = null;
						boolean typedefold = false;
				
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				typedefold = hastypedef;
						hastypedef = false;
				
			}
			{
			switch ( LA(1)) {
			case LITERAL_void:
			{
				match(LITERAL_void);
				if ( inputState.guessing==0 ) {
					types = Specifier.VOID;
				}
				break;
			}
			case LITERAL_char:
			{
				match(LITERAL_char);
				if ( inputState.guessing==0 ) {
					types = Specifier.CHAR;
				}
				break;
			}
			case LITERAL_short:
			{
				match(LITERAL_short);
				if ( inputState.guessing==0 ) {
					types = Specifier.SHORT;
				}
				break;
			}
			case LITERAL_int:
			{
				match(LITERAL_int);
				if ( inputState.guessing==0 ) {
					types = Specifier.INT;
				}
				break;
			}
			case LITERAL_long:
			{
				match(LITERAL_long);
				if ( inputState.guessing==0 ) {
					types = Specifier.LONG;
				}
				break;
			}
			case LITERAL_float:
			{
				match(LITERAL_float);
				if ( inputState.guessing==0 ) {
					types = Specifier.FLOAT;
				}
				break;
			}
			case LITERAL_double:
			{
				match(LITERAL_double);
				if ( inputState.guessing==0 ) {
					types = Specifier.DOUBLE;
				}
				break;
			}
			case LITERAL_signed:
			{
				match(LITERAL_signed);
				if ( inputState.guessing==0 ) {
					types = Specifier.SIGNED;
				}
				break;
			}
			case LITERAL_unsigned:
			{
				match(LITERAL_unsigned);
				if ( inputState.guessing==0 ) {
					types = Specifier.UNSIGNED;
				}
				break;
			}
			case LITERAL__Bool:
			{
				match(LITERAL__Bool);
				if ( inputState.guessing==0 ) {
					types = Specifier.CBOOL;
				}
				break;
			}
			case LITERAL__Complex:
			{
				match(LITERAL__Complex);
				if ( inputState.guessing==0 ) {
					types = Specifier.CCOMPLEX;
				}
				break;
			}
			case LITERAL__Imaginary:
			{
				match(LITERAL__Imaginary);
				if ( inputState.guessing==0 ) {
					types = Specifier.CIMAGINARY;
				}
				break;
			}
			case LITERAL_bool:
			{
				i = LT(1);
				match(LITERAL_bool);
				if ( inputState.guessing==0 ) {
					types = new UserSpecifier(new Identifier(i.getText()));
				}
				break;
			}
			case LITERAL_struct:
			case LITERAL_union:
			{
				types=structOrUnionSpecifier();
				{
				_loop43:
				do {
					if ((LA(1)==LITERAL_asm||LA(1)==LITERAL___attribute) && (LA(2)==LPAREN)) {
						attributeDecl();
					}
					else {
						break _loop43;
					}
					
				} while (true);
				}
				break;
			}
			case LITERAL_enum:
			{
				types=enumSpecifier();
				break;
			}
			case ID:
			{
				types=typedefName();
				break;
			}
			case LITERAL_typeof:
			{
				match(LITERAL_typeof);
				match(LPAREN);
				{
				boolean synPredMatched46 = false;
				if (((_tokenSet_22.member(LA(1))) && (_tokenSet_23.member(LA(2))))) {
					int _m46 = mark();
					synPredMatched46 = true;
					inputState.guessing++;
					try {
						{
						typeName();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched46 = false;
					}
					rewind(_m46);
					inputState.guessing--;
				}
				if ( synPredMatched46 ) {
					tyname=typeName();
				}
				else if ((_tokenSet_15.member(LA(1))) && (_tokenSet_24.member(LA(2)))) {
					expr1=expr();
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(RPAREN);
				break;
			}
			case LITERAL___complex:
			{
				match(LITERAL___complex);
				if ( inputState.guessing==0 ) {
					types = Specifier.DOUBLE;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
					hastypedef = typedefold;
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
		return types;
	}
	
	public final void attributeDecl() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL___attribute:
			{
				match(LITERAL___attribute);
				match(LPAREN);
				match(LPAREN);
				attributeList();
				match(RPAREN);
				match(RPAREN);
				break;
			}
			case LITERAL_asm:
			{
				match(LITERAL_asm);
				match(LPAREN);
				stringConst();
				match(RPAREN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_25);
			} else {
			  throw ex;
			}
		}
	}
	
	public final Specifier  functionStorageClassSpecifier() throws RecognitionException, TokenStreamException {
		Specifier type;
		
		type= null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_extern:
			{
				match(LITERAL_extern);
				if ( inputState.guessing==0 ) {
					type= Specifier.EXTERN;
				}
				break;
			}
			case LITERAL_static:
			{
				match(LITERAL_static);
				if ( inputState.guessing==0 ) {
					type= Specifier.STATIC;
				}
				break;
			}
			case LITERAL_inline:
			{
				match(LITERAL_inline);
				if ( inputState.guessing==0 ) {
					type= Specifier.INLINE;
				}
				break;
			}
			case LITERAL___global__:
			{
				match(LITERAL___global__);
				if ( inputState.guessing==0 ) {
					type= Specifier.GLOBAL;
				}
				break;
			}
			case LITERAL___device__:
			{
				match(LITERAL___device__);
				if ( inputState.guessing==0 ) {
					type= Specifier.DEVICE;
				}
				break;
			}
			case LITERAL___host__:
			{
				match(LITERAL___host__);
				if ( inputState.guessing==0 ) {
					type= Specifier.HOST;
				}
				break;
			}
			case LITERAL___kernel:
			{
				match(LITERAL___kernel);
				if ( inputState.guessing==0 ) {
					type= Specifier.OPENCL_KERNEL;
				}
				break;
			}
			case LITERAL___global:
			{
				match(LITERAL___global);
				if ( inputState.guessing==0 ) {
					type= Specifier.OPENCL_GLOBAL;
				}
				break;
			}
			case LITERAL___local:
			{
				match(LITERAL___local);
				if ( inputState.guessing==0 ) {
					type= Specifier.OPENCL_LOCAL;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop38:
			do {
				if ((LA(1)==LITERAL_asm||LA(1)==LITERAL___attribute) && (LA(2)==LPAREN)) {
					attributeDecl();
				}
				else {
					break _loop38;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_18);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	public final Specifier  structOrUnionSpecifier() throws RecognitionException, TokenStreamException {
		Specifier spec;
		
		Token  i = null;
		Token  l = null;
		Token  l1 = null;
		Token  sou3 = null;
		
							ClassDeclaration decls = null;
							String name=null;
							int type=0;
							spec = null;
							int linenum = 0;
						
		
		try {      // for error handling
			type=structOrUnion();
			{
			boolean synPredMatched52 = false;
			if (((LA(1)==ID) && (LA(2)==LCURLY))) {
				int _m52 = mark();
				synPredMatched52 = true;
				inputState.guessing++;
				try {
					{
					match(ID);
					match(LCURLY);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched52 = false;
				}
				rewind(_m52);
				inputState.guessing--;
			}
			if ( synPredMatched52 ) {
				i = LT(1);
				match(ID);
				l = LT(1);
				match(LCURLY);
				if ( inputState.guessing==0 ) {
					name = i.getText();linenum = i.getLine(); putPragma(i,symtab);
				}
				if ( inputState.guessing==0 ) {
					
						String sname = null;
												if(type == 1){
														decls = new ClassDeclaration(ClassDeclaration.STRUCT,new Identifier(name));
														spec = new UserSpecifier(new Identifier("struct "+name));
												}
												else{
														decls = new ClassDeclaration(ClassDeclaration.UNION,new Identifier(name));
														spec = new UserSpecifier(new Identifier("union "+name));
												}
					
											
				}
				{
				switch ( LA(1)) {
				case LITERAL_volatile:
				case LITERAL_struct:
				case LITERAL_union:
				case LITERAL_enum:
				case LITERAL___device__:
				case LITERAL_const:
				case LITERAL___shared__:
				case LITERAL___constant__:
				case LITERAL_void:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_long:
				case LITERAL_float:
				case LITERAL_double:
				case LITERAL_signed:
				case LITERAL_unsigned:
				case LITERAL__Bool:
				case LITERAL__Complex:
				case LITERAL__Imaginary:
				case LITERAL_bool:
				case LITERAL_typeof:
				case LITERAL___complex:
				case ID:
				{
					structDeclarationList(decls);
					break;
				}
				case RCURLY:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
							if(symtab instanceof ClassDeclaration){
								int si = symtabstack.size()-1;
								for(;si>=0;si--){
									if(!(symtabstack.get(si) instanceof ClassDeclaration)){
					
											((SymbolTable)symtabstack.get(si)).addDeclaration(decls);
										break;
									}
								}
							}
							else
								symtab.addDeclaration(decls);
					
				}
				match(RCURLY);
			}
			else if ((LA(1)==LCURLY)) {
				l1 = LT(1);
				match(LCURLY);
				if ( inputState.guessing==0 ) {
					
							name = getLexer().originalSource +"_"+ ((CToken)l1).getTokenNumber();
							name =name.replaceAll("[.]","_");
							name =name.replaceAll("-","_");
							linenum = l1.getLine(); putPragma(l1,symtab);
							if(type == 1){
												decls = new ClassDeclaration(ClassDeclaration.STRUCT,new Identifier(name));
												spec = new UserSpecifier(new Identifier("struct "+name));
											}
											else{
												decls = new ClassDeclaration(ClassDeclaration.UNION,new Identifier(name));
												spec = new UserSpecifier(new Identifier("union "+name));
											}
					
										
				}
				{
				switch ( LA(1)) {
				case LITERAL_volatile:
				case LITERAL_struct:
				case LITERAL_union:
				case LITERAL_enum:
				case LITERAL___device__:
				case LITERAL_const:
				case LITERAL___shared__:
				case LITERAL___constant__:
				case LITERAL_void:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_long:
				case LITERAL_float:
				case LITERAL_double:
				case LITERAL_signed:
				case LITERAL_unsigned:
				case LITERAL__Bool:
				case LITERAL__Complex:
				case LITERAL__Imaginary:
				case LITERAL_bool:
				case LITERAL_typeof:
				case LITERAL___complex:
				case ID:
				{
					structDeclarationList(decls);
					break;
				}
				case RCURLY:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
						if(symtab instanceof ClassDeclaration){
								int si = symtabstack.size()-1;
								for(;si>=0;si--){
									if(!(symtabstack.get(si) instanceof ClassDeclaration)){
					
										((SymbolTable)symtabstack.get(si)).addDeclaration(decls);
										break;
									}
								}
							}
							else
								symtab.addDeclaration(decls);
					
				}
				match(RCURLY);
			}
			else if ((LA(1)==ID) && (_tokenSet_21.member(LA(2)))) {
				sou3 = LT(1);
				match(ID);
				if ( inputState.guessing==0 ) {
					name = sou3.getText();linenum = sou3.getLine(); putPragma(sou3,symtab);
				}
				if ( inputState.guessing==0 ) {
					
											if(type == 1){
												spec = new UserSpecifier(new Identifier("struct "+name));
												decls = new ClassDeclaration(ClassDeclaration.STRUCT,new Identifier(name),true);
											}
											else{
												spec = new UserSpecifier(new Identifier("union "+name));
												decls = new ClassDeclaration(ClassDeclaration.UNION,new Identifier(name),true);
											}
											prev_decl = decls;
										
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
		return spec;
	}
	
	public final Specifier  enumSpecifier() throws RecognitionException, TokenStreamException {
		Specifier spec;
		
		Token  i = null;
		Token  el1 = null;
		Token  espec2 = null;
		
				 	cetus.hir.Enumeration espec = null;
				 	String enumN = null;
				 	List elist=null;
				 	spec = null;
				
		
		try {      // for error handling
			match(LITERAL_enum);
			{
			boolean synPredMatched78 = false;
			if (((LA(1)==ID) && (LA(2)==LCURLY))) {
				int _m78 = mark();
				synPredMatched78 = true;
				inputState.guessing++;
				try {
					{
					match(ID);
					match(LCURLY);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched78 = false;
				}
				rewind(_m78);
				inputState.guessing--;
			}
			if ( synPredMatched78 ) {
				i = LT(1);
				match(ID);
				match(LCURLY);
				elist=enumList();
				match(RCURLY);
				if ( inputState.guessing==0 ) {
					enumN =i.getText();
				}
			}
			else if ((LA(1)==LCURLY)) {
				el1 = LT(1);
				match(LCURLY);
				elist=enumList();
				match(RCURLY);
				if ( inputState.guessing==0 ) {
					
					
						enumN = getLexer().originalSource +"_"+ ((CToken)el1).getTokenNumber();
							enumN =enumN.replaceAll("[.]","_");
							enumN =enumN.replaceAll("-","_");
						
				}
			}
			else if ((LA(1)==ID) && (_tokenSet_21.member(LA(2)))) {
				espec2 = LT(1);
				match(ID);
				if ( inputState.guessing==0 ) {
					enumN =espec2.getText();
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
					if(elist != null){
				
						espec = new cetus.hir.Enumeration(new Identifier(enumN),elist);
						{
								if(symtab instanceof ClassDeclaration){
							int si = symtabstack.size()-1;
							for(;si>=0;si--){
								if(!(symtabstack.get(si) instanceof ClassDeclaration)){
				
									((SymbolTable)symtabstack.get(si)).addDeclaration(espec);
									break;
								}
							}
						}
						else
							symtab.addDeclaration(espec);
				
					}
					}
				
				
					spec = new UserSpecifier(new Identifier("enum "+enumN));
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
		return spec;
	}
	
	public final Specifier  typedefName() throws RecognitionException, TokenStreamException {
		Specifier name;
		
		Token  i = null;
		name = null;
		
		try {      // for error handling
			if (!( isTypedefName ( LT(1).getText() ) ))
			  throw new SemanticException(" isTypedefName ( LT(1).getText() ) ");
			i = LT(1);
			match(ID);
			if ( inputState.guessing==0 ) {
				name = new UserSpecifier(new Identifier(i.getText()));
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_21);
			} else {
			  throw ex;
			}
		}
		return name;
	}
	
	public final List  typeName() throws RecognitionException, TokenStreamException {
		List tname;
		
		
					tname=null;
		
					//Declarator decl= new Declarator();
					Declarator decl = null;
				
		
		try {      // for error handling
			tname=specifierQualifierList();
			{
			switch ( LA(1)) {
			case LPAREN:
			case STAR:
			case LBRACKET:
			{
				decl=nonemptyAbstractDeclarator();
				if ( inputState.guessing==0 ) {
					tname.add(decl);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
		return tname;
	}
	
	public final int  structOrUnion() throws RecognitionException, TokenStreamException {
		int type;
		
		type=0 ;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_struct:
			{
				match(LITERAL_struct);
				if ( inputState.guessing==0 ) {
					type = 1;
				}
				break;
			}
			case LITERAL_union:
			{
				match(LITERAL_union);
				if ( inputState.guessing==0 ) {
					type = 2;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_27);
			} else {
			  throw ex;
			}
		}
		return type;
	}
	
	public final void structDeclarationList(
		ClassDeclaration cdecl
	) throws RecognitionException, TokenStreamException {
		
		Declaration sdecl= null;/*SymbolTable prev_symtab = symtab;*/
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				
						enterSymtab(cdecl);
					
			}
			{
			int _cnt57=0;
			_loop57:
			do {
				if ((_tokenSet_22.member(LA(1)))) {
					sdecl=structDeclaration();
					if ( inputState.guessing==0 ) {
						if(sdecl != null )cdecl.addDeclaration(sdecl);
					}
				}
				else {
					if ( _cnt57>=1 ) { break _loop57; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt57++;
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				
						exitSymtab();
						/*symtab = prev_symtab;*/
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_28);
			} else {
			  throw ex;
			}
		}
	}
	
	public final Declaration  structDeclaration() throws RecognitionException, TokenStreamException {
		Declaration sdecl;
		
		
						List bsqlist=null;
			        	List bsdlist=null;
						sdecl=null;
					
		
		try {      // for error handling
			bsqlist=specifierQualifierList();
			bsdlist=structDeclaratorList();
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			int _cnt61=0;
			_loop61:
			do {
				if ((LA(1)==SEMI)) {
					match(SEMI);
				}
				else {
					if ( _cnt61>=1 ) { break _loop61; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt61++;
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				sdecl = new VariableDeclaration(bsqlist,bsdlist);
						hastypedef = false;
						
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_29);
			} else {
			  throw ex;
			}
		}
		return sdecl;
	}
	
	public final List  specifierQualifierList() throws RecognitionException, TokenStreamException {
		List sqlist;
		
		
				sqlist=new ArrayList();
							Specifier tspec=null;
							Specifier tqual=null;
						
		
		try {      // for error handling
			{
			int _cnt66=0;
			_loop66:
			do {
				boolean synPredMatched65 = false;
				if (((_tokenSet_12.member(LA(1))) && (_tokenSet_30.member(LA(2))))) {
					int _m65 = mark();
					synPredMatched65 = true;
					inputState.guessing++;
					try {
						{
						if ((LA(1)==LITERAL_struct) && (true)) {
							match(LITERAL_struct);
						}
						else if ((LA(1)==LITERAL_union) && (true)) {
							match(LITERAL_union);
						}
						else if ((LA(1)==LITERAL_enum) && (true)) {
							match(LITERAL_enum);
						}
						else if ((_tokenSet_12.member(LA(1))) && (true)) {
							typeSpecifier();
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
					}
					catch (RecognitionException pe) {
						synPredMatched65 = false;
					}
					rewind(_m65);
					inputState.guessing--;
				}
				if ( synPredMatched65 ) {
					tspec=typeSpecifier();
					if ( inputState.guessing==0 ) {
						sqlist.add(tspec);
					}
				}
				else if ((_tokenSet_11.member(LA(1)))) {
					tqual=typeQualifier();
					if ( inputState.guessing==0 ) {
						sqlist.add(tqual);
					}
				}
				else {
					if ( _cnt66>=1 ) { break _loop66; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt66++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		return sqlist;
	}
	
	public final List  structDeclaratorList() throws RecognitionException, TokenStreamException {
		List sdlist;
		
		
							sdlist = new ArrayList();
					Declarator sdecl=null;
				
		
		try {      // for error handling
			sdecl=structDeclarator();
			if ( inputState.guessing==0 ) {
				
							/*
							 * why am I getting a null value here ?
							 */
							if(sdecl != null)
							sdlist.add(sdecl);
						
			}
			{
			_loop69:
			do {
				if ((LA(1)==COMMA) && (_tokenSet_32.member(LA(2)))) {
					match(COMMA);
					sdecl=structDeclarator();
					if ( inputState.guessing==0 ) {
						
									sdlist.add(sdecl);
								
					}
				}
				else {
					break _loop69;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return sdlist;
	}
	
	public final Declarator  structDeclarator() throws RecognitionException, TokenStreamException {
		Declarator sdecl;
		
		
					sdecl=null;
					Expression expr1=null;
				
		
		try {      // for error handling
			{
			if ((_tokenSet_5.member(LA(1))) && (_tokenSet_34.member(LA(2)))) {
				sdecl=declarator();
			}
			else if ((_tokenSet_35.member(LA(1))) && (_tokenSet_36.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				expr1=constExpr();
				break;
			}
			case SEMI:
			case LITERAL_asm:
			case COMMA:
			case LITERAL___attribute:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop74:
			do {
				if ((LA(1)==LITERAL_asm||LA(1)==LITERAL___attribute)) {
					attributeDecl();
				}
				else {
					break _loop74;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return sdecl;
	}
	
	public final Expression  constExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		ret_expr = null;
		
		try {      // for error handling
			ret_expr=conditionalExpr();
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_37);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final List  enumList() throws RecognitionException, TokenStreamException {
		List elist;
		
		
			Declarator enum1=null;
			elist = new ArrayList();
		
		
		try {      // for error handling
			enum1=enumerator();
			if ( inputState.guessing==0 ) {
				elist.add(enum1);
			}
			{
			_loop81:
			do {
				if ((LA(1)==COMMA) && (LA(2)==ID)) {
					match(COMMA);
					enum1=enumerator();
					if ( inputState.guessing==0 ) {
						elist.add(enum1);
					}
				}
				else {
					break _loop81;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_28);
			} else {
			  throw ex;
			}
		}
		return elist;
	}
	
	public final Declarator  enumerator() throws RecognitionException, TokenStreamException {
		Declarator decl;
		
		Token  i = null;
		
			decl=null;Expression expr2=null;
		
			String val = null;
		
		
		try {      // for error handling
			i = LT(1);
			match(ID);
			if ( inputState.guessing==0 ) {
				
							val = i.getText();
							decl = new VariableDeclarator(new Identifier(val));
						
			}
			{
			switch ( LA(1)) {
			case ASSIGN:
			{
				match(ASSIGN);
				expr2=constExpr();
				if ( inputState.guessing==0 ) {
					
								decl.setInitializer(new Initializer(expr2));
							
				}
				break;
			}
			case RCURLY:
			case COMMA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_38);
			} else {
			  throw ex;
			}
		}
		return decl;
	}
	
	public final void attributeList() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			attribute();
			{
			_loop88:
			do {
				if ((LA(1)==COMMA) && ((LA(2) >= PREPROC_DIRECTIVE && LA(2) <= WideStringLiteral))) {
					match(COMMA);
					attribute();
				}
				else {
					break _loop88;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_26);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final String  stringConst() throws RecognitionException, TokenStreamException {
		String name;
		
		Token  sl = null;
		name = "";
		
		try {      // for error handling
			{
			int _cnt327=0;
			_loop327:
			do {
				if ((LA(1)==StringLiteral)) {
					sl = LT(1);
					match(StringLiteral);
					if ( inputState.guessing==0 ) {
						name += sl.getText();
					}
				}
				else {
					if ( _cnt327>=1 ) { break _loop327; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt327++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		return name;
	}
	
	public final void attribute() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop93:
			do {
				if ((_tokenSet_40.member(LA(1)))) {
					{
					match(_tokenSet_40);
					}
				}
				else if ((LA(1)==LPAREN)) {
					match(LPAREN);
					attributeList();
					match(RPAREN);
				}
				else {
					break _loop93;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_41);
			} else {
			  throw ex;
			}
		}
	}
	
	public final Declarator  initDecl() throws RecognitionException, TokenStreamException {
		Declarator decl;
		
		
							decl = null;
							//Initializer binit=null;
							Object binit = null;
							Expression expr1=null;
						
		
		try {      // for error handling
			decl=declarator();
			{
			_loop100:
			do {
				if ((LA(1)==LITERAL_asm||LA(1)==LITERAL___attribute)) {
					attributeDecl();
				}
				else {
					break _loop100;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case ASSIGN:
			{
				match(ASSIGN);
				binit=initializer();
				break;
			}
			case COLON:
			{
				match(COLON);
				expr1=expr();
				break;
			}
			case SEMI:
			case COMMA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
					if(binit instanceof Expression)
						binit = new Initializer((Expression)binit);
					if(binit != null){
					decl.setInitializer((Initializer)binit);
					/*
					System.out.println("Initializer " + decl.getClass());
					decl.print(System.out);
					System.out.print(" ");
					((Initializer)binit).print(System.out);
					System.out.println("");
					*/
					}
				
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return decl;
	}
	
	public final Object  initializer() throws RecognitionException, TokenStreamException {
		Object binit;
		
		binit = null; Expression expr1 = null;
			List ilist = null;
		
		try {      // for error handling
			{
			if ((_tokenSet_42.member(LA(1))) && (_tokenSet_43.member(LA(2)))) {
				{
				{
				boolean synPredMatched115 = false;
				if (((_tokenSet_44.member(LA(1))) && (_tokenSet_45.member(LA(2))))) {
					int _m115 = mark();
					synPredMatched115 = true;
					inputState.guessing++;
					try {
						{
						initializerElementLabel();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched115 = false;
					}
					rewind(_m115);
					inputState.guessing--;
				}
				if ( synPredMatched115 ) {
					initializerElementLabel();
				}
				else if ((_tokenSet_46.member(LA(1))) && (_tokenSet_47.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				{
				switch ( LA(1)) {
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					expr1=assignExpr();
					if ( inputState.guessing==0 ) {
						
								//binit = new Initializer(expr1);
								binit = expr1;
							
					}
					break;
				}
				case LCURLY:
				{
					ilist=lcurlyInitializer();
					if ( inputState.guessing==0 ) {
						binit = new Initializer(ilist);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
			}
			else if ((LA(1)==LCURLY) && (_tokenSet_48.member(LA(2)))) {
				ilist=lcurlyInitializer();
				if ( inputState.guessing==0 ) {
					binit = new Initializer(ilist);
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_49);
			} else {
			  throw ex;
			}
		}
		return binit;
	}
	
	public final List  pointerGroup() throws RecognitionException, TokenStreamException {
		List bp;
		
		
			bp = new ArrayList(1);
			Specifier temp = null;
			boolean b_const = false;
			boolean b_volatile = false;
		
		
		try {      // for error handling
			{
			int _cnt106=0;
			_loop106:
			do {
				if ((LA(1)==STAR)) {
					match(STAR);
					{
					_loop105:
					do {
						if ((_tokenSet_11.member(LA(1)))) {
							temp=typeQualifier();
							if ( inputState.guessing==0 ) {
								
													if(temp == Specifier.CONST)
														b_const = true;
								
													else if(temp == Specifier.VOLATILE)
														b_volatile = true;
												
							}
						}
						else {
							break _loop105;
						}
						
					} while (true);
					}
					if ( inputState.guessing==0 ) {
						
										if(b_const && b_volatile)
											bp.add(PointerSpecifier.CONST_VOLATILE);
										else if(b_const)
											bp.add(PointerSpecifier.CONST);
										else if(b_volatile)
											bp.add(PointerSpecifier.VOLATILE);
										else
											bp.add(PointerSpecifier.UNQUALIFIED);
									b_const = false;
									b_volatile = false;
									
					}
				}
				else {
					if ( _cnt106>=1 ) { break _loop106; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt106++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_50);
			} else {
			  throw ex;
			}
		}
		return bp;
	}
	
	public final List  idList() throws RecognitionException, TokenStreamException {
		List ilist;
		
		Token  idl1 = null;
		Token  idl2 = null;
		
			int i = 1;
			String name;
				Specifier temp = null;
			ilist = new ArrayList();
		
		
		try {      // for error handling
			idl1 = LT(1);
			match(ID);
			if ( inputState.guessing==0 ) {
				
							name = idl1.getText();
							ilist.add(new VariableDeclaration(new VariableDeclarator(new Identifier(name))));
				
						
			}
			{
			_loop109:
			do {
				if ((LA(1)==COMMA) && (LA(2)==ID)) {
					match(COMMA);
					idl2 = LT(1);
					match(ID);
					if ( inputState.guessing==0 ) {
						
										name = idl2.getText();
										ilist.add(new VariableDeclaration(new VariableDeclarator(new Identifier(name))));
								
					}
				}
				else {
					break _loop109;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_41);
			} else {
			  throw ex;
			}
		}
		return ilist;
	}
	
	public final void initializerElementLabel() throws RecognitionException, TokenStreamException {
		
		Expression expr1 = null,expr2=null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LBRACKET:
			{
				{
				match(LBRACKET);
				{
				boolean synPredMatched122 = false;
				if (((_tokenSet_15.member(LA(1))) && (_tokenSet_51.member(LA(2))))) {
					int _m122 = mark();
					synPredMatched122 = true;
					inputState.guessing++;
					try {
						{
						expr1=constExpr();
						match(VARARGS);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched122 = false;
					}
					rewind(_m122);
					inputState.guessing--;
				}
				if ( synPredMatched122 ) {
					expr1=rangeExpr();
				}
				else if ((_tokenSet_15.member(LA(1))) && (_tokenSet_52.member(LA(2)))) {
					expr2=constExpr();
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				match(RBRACKET);
				{
				switch ( LA(1)) {
				case ASSIGN:
				{
					match(ASSIGN);
					break;
				}
				case LCURLY:
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
				break;
			}
			case ID:
			{
				match(ID);
				match(COLON);
				break;
			}
			case DOT:
			{
				match(DOT);
				match(ID);
				match(ASSIGN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_46);
			} else {
			  throw ex;
			}
		}
	}
	
	public final Expression  assignExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		ret_expr = null; Expression expr1=null;AssignmentOperator code=null;
		
		try {      // for error handling
			ret_expr=conditionalExpr();
			{
			switch ( LA(1)) {
			case ASSIGN:
			case DIV_ASSIGN:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case MOD_ASSIGN:
			case RSHIFT_ASSIGN:
			case LSHIFT_ASSIGN:
			case BAND_ASSIGN:
			case BOR_ASSIGN:
			case BXOR_ASSIGN:
			{
				code=assignOperator();
				expr1=assignExpr();
				if ( inputState.guessing==0 ) {
					ret_expr = new AssignmentExpression(ret_expr,code,expr1);
				}
				break;
			}
			case SEMI:
			case RCURLY:
			case RPAREN:
			case COMMA:
			case COLON:
			case RBRACKET:
			case RKERNEL_LAUNCH:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_53);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final List  lcurlyInitializer() throws RecognitionException, TokenStreamException {
		List ilist;
		
		ilist = new ArrayList();
		
		try {      // for error handling
			match(LCURLY);
			{
			switch ( LA(1)) {
			case LCURLY:
			case LITERAL_asm:
			case LPAREN:
			case ID:
			case STAR:
			case LBRACKET:
			case DOT:
			case BAND:
			case PLUS:
			case MINUS:
			case INC:
			case DEC:
			case LITERAL_sizeof:
			case LITERAL___alignof:
			case BNOT:
			case LNOT:
			case LITERAL___real:
			case LITERAL___imag:
			case Number:
			case CharLiteral:
			case StringLiteral:
			{
				ilist=initializerList();
				{
				switch ( LA(1)) {
				case COMMA:
				{
					match(COMMA);
					break;
				}
				case RCURLY:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RCURLY);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		return ilist;
	}
	
	public final Expression  rangeExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		ret_expr = null;
		
		try {      // for error handling
			constExpr();
			match(VARARGS);
			constExpr();
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_55);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final List  initializerList() throws RecognitionException, TokenStreamException {
		List ilist;
		
		Object init = null; ilist = new ArrayList();
		
		try {      // for error handling
			{
			init=initializer();
			if ( inputState.guessing==0 ) {
				ilist.add(init);
			}
			}
			{
			_loop130:
			do {
				if ((LA(1)==COMMA) && (_tokenSet_42.member(LA(2)))) {
					match(COMMA);
					init=initializer();
					if ( inputState.guessing==0 ) {
						ilist.add(init);
					}
				}
				else {
					break _loop130;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_38);
			} else {
			  throw ex;
			}
		}
		return ilist;
	}
	
	public final List  declaratorParamaterList() throws RecognitionException, TokenStreamException {
		List plist;
		
		
			plist = new ArrayList();
		
		
		try {      // for error handling
			match(LPAREN);
			{
			boolean synPredMatched141 = false;
			if (((_tokenSet_1.member(LA(1))) && (_tokenSet_56.member(LA(2))))) {
				int _m141 = mark();
				synPredMatched141 = true;
				inputState.guessing++;
				try {
					{
					declSpecifiers();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched141 = false;
				}
				rewind(_m141);
				inputState.guessing--;
			}
			if ( synPredMatched141 ) {
				plist=parameterTypeList();
			}
			else if ((_tokenSet_57.member(LA(1))) && (_tokenSet_58.member(LA(2)))) {
				{
				switch ( LA(1)) {
				case ID:
				{
					plist=idList();
					break;
				}
				case RPAREN:
				case COMMA:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_58);
			} else {
			  throw ex;
			}
		}
		return plist;
	}
	
	public final List  parameterTypeList() throws RecognitionException, TokenStreamException {
		List ptlist;
		
		
			ptlist = new ArrayList();
			Declaration pdecl = null;
			
		
		try {      // for error handling
			pdecl=parameterDeclaration();
			if ( inputState.guessing==0 ) {
				ptlist.add(pdecl);
			}
			{
			_loop147:
			do {
				if ((LA(1)==SEMI||LA(1)==COMMA) && (_tokenSet_1.member(LA(2)))) {
					{
					switch ( LA(1)) {
					case COMMA:
					{
						match(COMMA);
						break;
					}
					case SEMI:
					{
						match(SEMI);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					pdecl=parameterDeclaration();
					if ( inputState.guessing==0 ) {
						ptlist.add(pdecl);
					}
				}
				else {
					break _loop147;
				}
				
			} while (true);
			}
			{
			if ((LA(1)==SEMI||LA(1)==COMMA) && (LA(2)==VARARGS)) {
				{
				switch ( LA(1)) {
				case COMMA:
				{
					match(COMMA);
					break;
				}
				case SEMI:
				{
					match(SEMI);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(VARARGS);
				if ( inputState.guessing==0 ) {
					ptlist.add(new VariableDeclaration(new VariableDeclarator(new Identifier("..."))));
				}
			}
			else if ((LA(1)==RPAREN||LA(1)==COMMA) && (_tokenSet_58.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_41);
			} else {
			  throw ex;
			}
		}
		return ptlist;
	}
	
	public final Declaration  parameterDeclaration() throws RecognitionException, TokenStreamException {
		Declaration pdecl;
		
		
						pdecl =null;
						List dspec = null;
						Declarator decl = null;
						boolean prevhastypedef = hastypedef;
						hastypedef = false;
					
		
		try {      // for error handling
			dspec=declSpecifiers();
			{
			boolean synPredMatched153 = false;
			if (((_tokenSet_5.member(LA(1))) && (_tokenSet_59.member(LA(2))))) {
				int _m153 = mark();
				synPredMatched153 = true;
				inputState.guessing++;
				try {
					{
					declarator();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched153 = false;
				}
				rewind(_m153);
				inputState.guessing--;
			}
			if ( synPredMatched153 ) {
				decl=declarator();
			}
			else if ((_tokenSet_60.member(LA(1))) && (_tokenSet_61.member(LA(2)))) {
				decl=nonemptyAbstractDeclarator();
			}
			else if ((_tokenSet_62.member(LA(1)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				
					if(decl != null){
				
						pdecl = new VariableDeclaration(dspec,decl);
						if(isFuncDef){
							currproc.add(pdecl);
						}
					}
					else
						pdecl = new VariableDeclaration(dspec,new VariableDeclarator(new Identifier("")));
													hastypedef = prevhastypedef;
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_62);
			} else {
			  throw ex;
			}
		}
		return pdecl;
	}
	
	public final Declarator  nonemptyAbstractDeclarator() throws RecognitionException, TokenStreamException {
		Declarator adecl;
		
		
					Expression expr1=null;
					List plist=null;
					List bp = null;
		
					Declarator tdecl = null;
		
		
						Specifier aspec = null;
						boolean isArraySpec = false;
						boolean isNested = false;
						List llist = new ArrayList(1);
						List tlist = null;
						boolean empty = true;
					adecl = null;
				
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case STAR:
			{
				bp=pointerGroup();
				{
				_loop284:
				do {
					switch ( LA(1)) {
					case LPAREN:
					{
						{
						match(LPAREN);
						{
						switch ( LA(1)) {
						case LITERAL_typedef:
						case LITERAL_asm:
						case LITERAL_volatile:
						case LITERAL_struct:
						case LITERAL_union:
						case LITERAL_enum:
						case LITERAL_auto:
						case LITERAL_register:
						case LITERAL_extern:
						case LITERAL_static:
						case LITERAL_inline:
						case LITERAL___global__:
						case LITERAL___device__:
						case LITERAL___host__:
						case LITERAL___kernel:
						case LITERAL___global:
						case LITERAL___local:
						case LITERAL_const:
						case LITERAL___shared__:
						case LITERAL___constant__:
						case LITERAL_void:
						case LITERAL_char:
						case LITERAL_short:
						case LITERAL_int:
						case LITERAL_long:
						case LITERAL_float:
						case LITERAL_double:
						case LITERAL_signed:
						case LITERAL_unsigned:
						case LITERAL__Bool:
						case LITERAL__Complex:
						case LITERAL__Imaginary:
						case LITERAL_bool:
						case LITERAL_typeof:
						case LPAREN:
						case LITERAL___complex:
						case ID:
						case LITERAL___attribute:
						case STAR:
						case LBRACKET:
						{
							{
							switch ( LA(1)) {
							case LPAREN:
							case STAR:
							case LBRACKET:
							{
								{
								tdecl=nonemptyAbstractDeclarator();
								}
								break;
							}
							case LITERAL_typedef:
							case LITERAL_asm:
							case LITERAL_volatile:
							case LITERAL_struct:
							case LITERAL_union:
							case LITERAL_enum:
							case LITERAL_auto:
							case LITERAL_register:
							case LITERAL_extern:
							case LITERAL_static:
							case LITERAL_inline:
							case LITERAL___global__:
							case LITERAL___device__:
							case LITERAL___host__:
							case LITERAL___kernel:
							case LITERAL___global:
							case LITERAL___local:
							case LITERAL_const:
							case LITERAL___shared__:
							case LITERAL___constant__:
							case LITERAL_void:
							case LITERAL_char:
							case LITERAL_short:
							case LITERAL_int:
							case LITERAL_long:
							case LITERAL_float:
							case LITERAL_double:
							case LITERAL_signed:
							case LITERAL_unsigned:
							case LITERAL__Bool:
							case LITERAL__Complex:
							case LITERAL__Imaginary:
							case LITERAL_bool:
							case LITERAL_typeof:
							case LITERAL___complex:
							case ID:
							case LITERAL___attribute:
							{
								plist=parameterTypeList();
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							if ( inputState.guessing==0 ) {
								empty = false;
							}
							break;
						}
						case RPAREN:
						case COMMA:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						{
						switch ( LA(1)) {
						case COMMA:
						{
							match(COMMA);
							break;
						}
						case RPAREN:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						match(RPAREN);
						}
						if ( inputState.guessing==0 ) {
							
							
							if(empty)
								plist = new ArrayList();
							empty = true;
							
						}
						break;
					}
					case LBRACKET:
					{
						{
						match(LBRACKET);
						{
						switch ( LA(1)) {
						case LITERAL_asm:
						case LPAREN:
						case ID:
						case STAR:
						case BAND:
						case PLUS:
						case MINUS:
						case INC:
						case DEC:
						case LITERAL_sizeof:
						case LITERAL___alignof:
						case BNOT:
						case LNOT:
						case LITERAL___real:
						case LITERAL___imag:
						case Number:
						case CharLiteral:
						case StringLiteral:
						{
							expr1=expr();
							break;
						}
						case RBRACKET:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						match(RBRACKET);
						}
						if ( inputState.guessing==0 ) {
							
									isArraySpec = true;
							
										llist.add(expr1);
							
								
						}
						break;
					}
					default:
					{
						break _loop284;
					}
					}
				} while (true);
				}
				break;
			}
			case LPAREN:
			case LBRACKET:
			{
				{
				int _cnt293=0;
				_loop293:
				do {
					switch ( LA(1)) {
					case LPAREN:
					{
						{
						match(LPAREN);
						{
						switch ( LA(1)) {
						case LITERAL_typedef:
						case LITERAL_asm:
						case LITERAL_volatile:
						case LITERAL_struct:
						case LITERAL_union:
						case LITERAL_enum:
						case LITERAL_auto:
						case LITERAL_register:
						case LITERAL_extern:
						case LITERAL_static:
						case LITERAL_inline:
						case LITERAL___global__:
						case LITERAL___device__:
						case LITERAL___host__:
						case LITERAL___kernel:
						case LITERAL___global:
						case LITERAL___local:
						case LITERAL_const:
						case LITERAL___shared__:
						case LITERAL___constant__:
						case LITERAL_void:
						case LITERAL_char:
						case LITERAL_short:
						case LITERAL_int:
						case LITERAL_long:
						case LITERAL_float:
						case LITERAL_double:
						case LITERAL_signed:
						case LITERAL_unsigned:
						case LITERAL__Bool:
						case LITERAL__Complex:
						case LITERAL__Imaginary:
						case LITERAL_bool:
						case LITERAL_typeof:
						case LPAREN:
						case LITERAL___complex:
						case ID:
						case LITERAL___attribute:
						case STAR:
						case LBRACKET:
						{
							{
							switch ( LA(1)) {
							case LPAREN:
							case STAR:
							case LBRACKET:
							{
								{
								tdecl=nonemptyAbstractDeclarator();
								}
								break;
							}
							case LITERAL_typedef:
							case LITERAL_asm:
							case LITERAL_volatile:
							case LITERAL_struct:
							case LITERAL_union:
							case LITERAL_enum:
							case LITERAL_auto:
							case LITERAL_register:
							case LITERAL_extern:
							case LITERAL_static:
							case LITERAL_inline:
							case LITERAL___global__:
							case LITERAL___device__:
							case LITERAL___host__:
							case LITERAL___kernel:
							case LITERAL___global:
							case LITERAL___local:
							case LITERAL_const:
							case LITERAL___shared__:
							case LITERAL___constant__:
							case LITERAL_void:
							case LITERAL_char:
							case LITERAL_short:
							case LITERAL_int:
							case LITERAL_long:
							case LITERAL_float:
							case LITERAL_double:
							case LITERAL_signed:
							case LITERAL_unsigned:
							case LITERAL__Bool:
							case LITERAL__Complex:
							case LITERAL__Imaginary:
							case LITERAL_bool:
							case LITERAL_typeof:
							case LITERAL___complex:
							case ID:
							case LITERAL___attribute:
							{
								plist=parameterTypeList();
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							if ( inputState.guessing==0 ) {
								empty = false;
							}
							break;
						}
						case RPAREN:
						case COMMA:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						{
						switch ( LA(1)) {
						case COMMA:
						{
							match(COMMA);
							break;
						}
						case RPAREN:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						match(RPAREN);
						}
						if ( inputState.guessing==0 ) {
							
							
							if(empty)
								plist = new ArrayList();
							empty = true;
							
						}
						break;
					}
					case LBRACKET:
					{
						{
						match(LBRACKET);
						{
						switch ( LA(1)) {
						case LITERAL_asm:
						case LPAREN:
						case ID:
						case STAR:
						case BAND:
						case PLUS:
						case MINUS:
						case INC:
						case DEC:
						case LITERAL_sizeof:
						case LITERAL___alignof:
						case BNOT:
						case LNOT:
						case LITERAL___real:
						case LITERAL___imag:
						case Number:
						case CharLiteral:
						case StringLiteral:
						{
							expr1=expr();
							break;
						}
						case RBRACKET:
						{
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
						match(RBRACKET);
						}
						if ( inputState.guessing==0 ) {
							
									isArraySpec = true;
							
										llist.add(expr1);
							
								
						}
						break;
					}
					default:
					{
						if ( _cnt293>=1 ) { break _loop293; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					}
					_cnt293++;
				} while (true);
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
					// this is wrong
					/*
					if(tdecl == null){
									adecl = new Declarator(bp, new Identifier(""), plist, tlist,null) ;
								}
					else
						adecl = new Declarator(bp, tdecl, plist) ;
				
						*/
						if(isArraySpec){
											/*
											 * []+
											 */
											aspec = new ArraySpecifier(llist);
											//aspec = new ArraySpecifier();
											tlist = new ArrayList();
											tlist.add(aspec);
									}
						{ Identifier idex = null;
				
							// nested declarator (tlist == null ?)
				
							if(bp == null)
								bp = new LinkedList();
										if(tdecl != null) // assume tlist == null
										{
											//assert tlist == null : "Assertion (tlist == null) failed 2";
											if(tlist == null)
										tlist = new LinkedList();
											//if(plist != null){
												adecl = new NestedDeclarator(bp,tdecl,plist,tlist);
											//}
											//else
											//	decl = new NestedDeclarator(bp,tdecl,new LinkedList(),tlist);
										}
										else{
											idex = new Identifier("");
											if(plist != null) // assume tlist == null
										adecl = new ProcedureDeclarator(bp,idex,plist);
								else{
									if(tlist != null)
										adecl = new VariableDeclarator(bp,idex,tlist);
									else
										adecl = new VariableDeclarator(bp,idex);
								}
										}
						}
				
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_62);
			} else {
			  throw ex;
			}
		}
		return adecl;
	}
	
	public final CompoundStatement  compoundStatement() throws RecognitionException, TokenStreamException {
		CompoundStatement stmt;
		
		Token  lcur = null;
		Token  rcur = null;
		
						stmt = null;
						int linenum = 0;
						SymbolTable prev_symtab = null;
						CompoundStatement prev_cstmt = null;
						Statement statb = null;
						
		
		try {      // for error handling
			lcur = LT(1);
			match(LCURLY);
			if ( inputState.guessing==0 ) {
				linenum = lcur.getLine();
			}
			if ( inputState.guessing==0 ) {
				
				
					 prev_symtab = symtab;
				
				
								 prev_cstmt = curr_cstmt;
					stmt = new CompoundStatement();
										/*
										if(symtab instanceof CompoundStatement){
				
										}
										else if(symtab instanceof Procedure){
				
										}
										*/
					enterSymtab(stmt);
					stmt.setLineNumber(linenum);
					putPragma(lcur,prev_symtab);
					curr_cstmt = stmt;
					/*symtab = stmt;*/
				
			}
			{
			_loop190:
			do {
				boolean synPredMatched185 = false;
				if (((_tokenSet_63.member(LA(1))) && (_tokenSet_2.member(LA(2))))) {
					int _m185 = mark();
					synPredMatched185 = true;
					inputState.guessing++;
					try {
						{
						if ((LA(1)==LITERAL_typedef) && (true)) {
							match(LITERAL_typedef);
						}
						else if ((LA(1)==LITERAL___label__)) {
							match(LITERAL___label__);
						}
						else if ((_tokenSet_1.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
							declaration();
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						
						}
					}
					catch (RecognitionException pe) {
						synPredMatched185 = false;
					}
					rewind(_m185);
					inputState.guessing--;
				}
				if ( synPredMatched185 ) {
					{
					declarationList();
					}
				}
				else {
					boolean synPredMatched188 = false;
					if (((_tokenSet_64.member(LA(1))) && (_tokenSet_65.member(LA(2))))) {
						int _m188 = mark();
						synPredMatched188 = true;
						inputState.guessing++;
						try {
							{
							nestedFunctionDef();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched188 = false;
						}
						rewind(_m188);
						inputState.guessing--;
					}
					if ( synPredMatched188 ) {
						nestedFunctionDef();
					}
					else if ((_tokenSet_66.member(LA(1))) && (_tokenSet_67.member(LA(2)))) {
						{
						statb=statement();
						if ( inputState.guessing==0 ) {
							curr_cstmt.addStatement(statb);
						}
						}
					}
					else {
						break _loop190;
					}
					}
				} while (true);
				}
				rcur = LT(1);
				match(RCURLY);
				if ( inputState.guessing==0 ) {
					
						linenum = rcur.getLine();
						putPragma(rcur,symtab);
					
					curr_cstmt = prev_cstmt;
					/*
					symtab = prev_symtab;
					*/
					exitSymtab();
					
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_68);
				} else {
				  throw ex;
				}
			}
			return stmt;
		}
		
	public final void declarationList() throws RecognitionException, TokenStreamException {
		
		Declaration decl=null;ArrayList tlist = new ArrayList();
		
		try {      // for error handling
			{
			int _cnt172=0;
			_loop172:
			do {
				if ((LA(1)==LITERAL___label__) && (LA(2)==ID)) {
					localLabelDeclaration();
				}
				else {
					boolean synPredMatched171 = false;
					if (((_tokenSet_1.member(LA(1))) && (_tokenSet_2.member(LA(2))))) {
						int _m171 = mark();
						synPredMatched171 = true;
						inputState.guessing++;
						try {
							{
							declarationPredictor();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched171 = false;
						}
						rewind(_m171);
						inputState.guessing--;
					}
					if ( synPredMatched171 ) {
						decl=declaration();
						if ( inputState.guessing==0 ) {
							if(decl != null ) curr_cstmt.addDeclaration(decl);
						}
					}
					else {
						if ( _cnt172>=1 ) { break _loop172; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					}
					_cnt172++;
				} while (true);
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_69);
				} else {
				  throw ex;
				}
			}
		}
		
	public final void localLabelDeclaration() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			match(LITERAL___label__);
			match(ID);
			{
			_loop178:
			do {
				if ((LA(1)==COMMA) && (LA(2)==ID)) {
					match(COMMA);
					match(ID);
				}
				else {
					break _loop178;
				}
				
			} while (true);
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case SEMI:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			int _cnt181=0;
			_loop181:
			do {
				if ((LA(1)==SEMI) && (_tokenSet_69.member(LA(2)))) {
					match(SEMI);
				}
				else {
					if ( _cnt181>=1 ) { break _loop181; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt181++;
			} while (true);
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_69);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void declarationPredictor() throws RecognitionException, TokenStreamException {
		
		Declaration decl=null;
		
		try {      // for error handling
			{
			if ((LA(1)==LITERAL_typedef) && (LA(2)==EOF)) {
				match(LITERAL_typedef);
			}
			else if ((_tokenSet_1.member(LA(1))) && (_tokenSet_2.member(LA(2)))) {
				decl=declaration();
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void nestedFunctionDef() throws RecognitionException, TokenStreamException {
		
		Declarator decl=null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_auto:
			{
				match(LITERAL_auto);
				break;
			}
			case LITERAL_asm:
			case LITERAL_volatile:
			case LITERAL_struct:
			case LITERAL_union:
			case LITERAL_enum:
			case LITERAL_extern:
			case LITERAL_static:
			case LITERAL_inline:
			case LITERAL___global__:
			case LITERAL___device__:
			case LITERAL___host__:
			case LITERAL___kernel:
			case LITERAL___global:
			case LITERAL___local:
			case LITERAL_const:
			case LITERAL___shared__:
			case LITERAL___constant__:
			case LITERAL_void:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_long:
			case LITERAL_float:
			case LITERAL_double:
			case LITERAL_signed:
			case LITERAL_unsigned:
			case LITERAL__Bool:
			case LITERAL__Complex:
			case LITERAL__Imaginary:
			case LITERAL_bool:
			case LITERAL_typeof:
			case LPAREN:
			case LITERAL___complex:
			case ID:
			case LITERAL___attribute:
			case STAR:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			boolean synPredMatched195 = false;
			if (((_tokenSet_9.member(LA(1))) && (_tokenSet_10.member(LA(2))))) {
				int _m195 = mark();
				synPredMatched195 = true;
				inputState.guessing++;
				try {
					{
					functionDeclSpecifiers();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched195 = false;
				}
				rewind(_m195);
				inputState.guessing--;
			}
			if ( synPredMatched195 ) {
				functionDeclSpecifiers();
			}
			else if ((_tokenSet_5.member(LA(1))) && (_tokenSet_65.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			decl=declarator();
			{
			_loop197:
			do {
				if ((_tokenSet_1.member(LA(1)))) {
					declaration();
				}
				else {
					break _loop197;
				}
				
			} while (true);
			}
			compoundStatement();
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_69);
			} else {
			  throw ex;
			}
		}
	}
	
	public final Statement  statement() throws RecognitionException, TokenStreamException {
		Statement statb;
		
		Token  tsemi = null;
		Token  exprsemi = null;
		Token  twhile = null;
		Token  tdo = null;
		Token  tfor = null;
		Token  tgoto = null;
		Token  gotoTarget = null;
		Token  tcontinue = null;
		Token  tbreak = null;
		Token  treturn = null;
		Token  lid = null;
		Token  tcase = null;
		Token  tdefault = null;
		Token  tif = null;
		Token  tswitch = null;
		Expression stmtb_expr;
					statb = null;
					Expression expr1=null, expr2=null, expr3=null;
			         Statement stmt1=null,stmt2=null;
			         int a=0;
			         int sline = 0;
		
		
				
		
		try {      // for error handling
			switch ( LA(1)) {
			case SEMI:
			{
				tsemi = LT(1);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								sline = tsemi.getLine();
								statb = new NullStatement();
								putPragma(tsemi,symtab);
					
							
				}
				break;
			}
			case LCURLY:
			{
				statb=compoundStatement();
				break;
			}
			case LITERAL_while:
			{
				twhile = LT(1);
				match(LITERAL_while);
				match(LPAREN);
				if ( inputState.guessing==0 ) {
					
								sline = twhile.getLine();
								putPragma(twhile,symtab);
							
				}
				expr1=expr();
				match(RPAREN);
				stmt1=statement();
				if ( inputState.guessing==0 ) {
					
										statb = new WhileLoop(expr1, stmt1);
										statb.setLineNumber(sline);
					
					
					
									
				}
				break;
			}
			case LITERAL_do:
			{
				tdo = LT(1);
				match(LITERAL_do);
				if ( inputState.guessing==0 ) {
					
								sline = tdo.getLine();
								putPragma(tdo,symtab);
							
				}
				stmt1=statement();
				match(LITERAL_while);
				match(LPAREN);
				expr1=expr();
				match(RPAREN);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								statb = new DoLoop(stmt1, expr1);
										statb.setLineNumber(sline);
					
					
							
				}
				break;
			}
			case LITERAL_for:
			{
				tfor = LT(1);
				match(LITERAL_for);
				if ( inputState.guessing==0 ) {
					
								sline = tfor.getLine();
								putPragma(tfor,symtab);
					
							
				}
				match(LPAREN);
				{
				switch ( LA(1)) {
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					expr1=expr();
					break;
				}
				case SEMI:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(SEMI);
				{
				switch ( LA(1)) {
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					expr2=expr();
					break;
				}
				case SEMI:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(SEMI);
				{
				switch ( LA(1)) {
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					expr3=expr();
					break;
				}
				case RPAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RPAREN);
				stmt1=statement();
				if ( inputState.guessing==0 ) {
						if(expr1 != null)
										statb = new ForLoop(new ExpressionStatement(expr1),expr2,expr3,stmt1);
						else
						statb = new ForLoop(new NullStatement(),expr2,expr3,stmt1);
						statb.setLineNumber(sline);
					
					
							
				}
				break;
			}
			case LITERAL_goto:
			{
				tgoto = LT(1);
				match(LITERAL_goto);
				if ( inputState.guessing==0 ) {
					
								sline = tgoto.getLine();
								putPragma(tgoto,symtab);
							
				}
				gotoTarget = LT(1);
				match(ID);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								/*
								BaseDeclaration target = null;
								Object o = null;
								o =functionSymtab.get(gotoTarget.getText());
								if(o == null){
									target = new BaseDeclaration(new BaseSpecifier(),null);
									functionSymtab.put(gotoTarget.getText(),target);
								}
								else
									target = (Declaration)o;
					
								statb = new GotoStmt(target);
								*/
								statb = new GotoStatement(new Identifier(gotoTarget.getText()));
					
								statb.setLineNumber(sline);
					
					
							
				}
				break;
			}
			case LITERAL_continue:
			{
				tcontinue = LT(1);
				match(LITERAL_continue);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								sline = tcontinue.getLine();
					
								statb = new ContinueStatement();
								statb.setLineNumber(sline);
								putPragma(tcontinue,symtab);
					
							
				}
				break;
			}
			case LITERAL_break:
			{
				tbreak = LT(1);
				match(LITERAL_break);
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								sline = tbreak.getLine();
					
								statb = new BreakStatement();
								statb.setLineNumber(sline);
								putPragma(tbreak,symtab);
					
							
				}
				break;
			}
			case LITERAL_return:
			{
				treturn = LT(1);
				match(LITERAL_return);
				if ( inputState.guessing==0 ) {
					
								sline = treturn.getLine();
					
							
				}
				{
				switch ( LA(1)) {
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					expr1=expr();
					break;
				}
				case SEMI:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(SEMI);
				if ( inputState.guessing==0 ) {
					
								if(expr1 != null)
									statb=new ReturnStatement(expr1);
								else
									statb=new ReturnStatement();
								statb.setLineNumber(sline);
								putPragma(treturn,symtab);
					
							
				}
				break;
			}
			case LITERAL_case:
			{
				tcase = LT(1);
				match(LITERAL_case);
				if ( inputState.guessing==0 ) {
					
								sline = tcase.getLine();
					
							
				}
				{
				boolean synPredMatched209 = false;
				if (((_tokenSet_15.member(LA(1))) && (_tokenSet_51.member(LA(2))))) {
					int _m209 = mark();
					synPredMatched209 = true;
					inputState.guessing++;
					try {
						{
						constExpr();
						match(VARARGS);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched209 = false;
					}
					rewind(_m209);
					inputState.guessing--;
				}
				if ( synPredMatched209 ) {
					expr1=rangeExpr();
				}
				else if ((_tokenSet_15.member(LA(1))) && (_tokenSet_70.member(LA(2)))) {
					expr1=constExpr();
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
								statb = new Case(expr1);
										statb.setLineNumber(sline);
										putPragma(tcase,symtab);
					
							
				}
				match(COLON);
				{
				if ((_tokenSet_66.member(LA(1))) && (_tokenSet_71.member(LA(2)))) {
					stmt1=statement();
					if ( inputState.guessing==0 ) {
						
									CompoundStatement cstmt = new CompoundStatement();
									cstmt.addStatement(statb);
									statb = cstmt;
									cstmt.addStatement(stmt1);
								
					}
				}
				else if ((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case LITERAL_default:
			{
				tdefault = LT(1);
				match(LITERAL_default);
				if ( inputState.guessing==0 ) {
					
								sline = tdefault.getLine();
							
				}
				if ( inputState.guessing==0 ) {
					
								statb = new Default();
										statb.setLineNumber(sline);
										putPragma(tdefault,symtab);
					
							
				}
				match(COLON);
				{
				if ((_tokenSet_66.member(LA(1))) && (_tokenSet_71.member(LA(2)))) {
					stmt1=statement();
					if ( inputState.guessing==0 ) {
						
									CompoundStatement cstmt = new CompoundStatement();
									cstmt.addStatement(statb);
									statb = cstmt;
									cstmt.addStatement(stmt1);
								
					}
				}
				else if ((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case LITERAL_if:
			{
				tif = LT(1);
				match(LITERAL_if);
				if ( inputState.guessing==0 ) {
					
								sline = tif.getLine();
								putPragma(tif,symtab);
							
				}
				match(LPAREN);
				expr1=expr();
				match(RPAREN);
				stmt1=statement();
				{
				if ((LA(1)==LITERAL_else) && (_tokenSet_66.member(LA(2)))) {
					match(LITERAL_else);
					stmt2=statement();
				}
				else if ((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				if ( inputState.guessing==0 ) {
					
							 			if(stmt2 != null)
								 			statb = new IfStatement(expr1,stmt1,stmt2);
								 		else
								 			statb = new IfStatement(expr1,stmt1);
								 		statb.setLineNumber(sline);
					
					
							 		
				}
				break;
			}
			case LITERAL_switch:
			{
				tswitch = LT(1);
				match(LITERAL_switch);
				match(LPAREN);
				if ( inputState.guessing==0 ) {
					
								sline = tswitch.getLine();
							
				}
				expr1=expr();
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					
								statb = new SwitchStatement(expr1);
										statb.setLineNumber(sline);
									putPragma(tswitch,symtab);
					
							
				}
				stmt1=statement();
				if ( inputState.guessing==0 ) {
					
								((SwitchStatement)statb).setBody((CompoundStatement)stmt1);
							
				}
				break;
			}
			default:
				if ((_tokenSet_15.member(LA(1))) && (_tokenSet_74.member(LA(2)))) {
					stmtb_expr=expr();
					exprsemi = LT(1);
					match(SEMI);
					if ( inputState.guessing==0 ) {
						
									sline = exprsemi.getLine();
									putPragma(exprsemi,symtab);
									/*
									 *	I really shouldn't do this test
									 */
						
									statb = new ExpressionStatement(stmtb_expr);
						
						
								
					}
				}
				else if ((LA(1)==ID) && (LA(2)==COLON)) {
					lid = LT(1);
					match(ID);
					match(COLON);
					if ( inputState.guessing==0 ) {
						
									sline = lid.getLine();
						
								
					}
					if ( inputState.guessing==0 ) {
						
									Object o = null;
									Declaration target = null;
						
									//statb = new Label(new Identifier(lid.getText()));
											statb = new Label(new Identifier(lid.getText()));
											/*
											o = functionSymtab.get(lid.getText());
											// place holder is there
											if(o != null){
												target = (Declaration)o;
												target.getSpecifier().add(statb);
											}
											else{
												Specifier spec = new Specifier();
												spec.add(statb);
												target = new Declaration(spec,null);
												functionSymtab.put(lid.getText(),target);
											}
											*/
											statb.setLineNumber(sline);
											putPragma(lid,symtab);
						
								
					}
					{
					if ((_tokenSet_66.member(LA(1))) && (_tokenSet_71.member(LA(2)))) {
						stmt1=statement();
						if ( inputState.guessing==0 ) {
							
										CompoundStatement cstmt = new CompoundStatement();
										cstmt.addStatement(statb);
										statb = cstmt;
										cstmt.addStatement(stmt1);
									
						}
					}
					else if ((_tokenSet_72.member(LA(1))) && (_tokenSet_73.member(LA(2)))) {
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_72);
			} else {
			  throw ex;
			}
		}
		return statb;
	}
	
	public final void statementList() throws RecognitionException, TokenStreamException {
		
		
						Statement statb = null;
						
		
		try {      // for error handling
			{
			int _cnt200=0;
			_loop200:
			do {
				if ((_tokenSet_66.member(LA(1)))) {
					statb=statement();
					if ( inputState.guessing==0 ) {
						curr_cstmt.addStatement(statb);
					}
				}
				else {
					if ( _cnt200>=1 ) { break _loop200; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt200++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	public final Expression  conditionalExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		ret_expr=null;
						Expression expr1=null,expr2=null,expr3=null;
		
		try {      // for error handling
			expr1=logicalOrExpr();
			if ( inputState.guessing==0 ) {
				ret_expr = expr1;
			}
			{
			switch ( LA(1)) {
			case QUESTION:
			{
				match(QUESTION);
				{
				switch ( LA(1)) {
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					expr2=expr();
					break;
				}
				case COLON:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(COLON);
				expr3=conditionalExpr();
				if ( inputState.guessing==0 ) {
						//if(expr2 == null) expr2 = new Expression();
										ret_expr = new ConditionalExpression(expr1,expr2,expr3);
				}
				break;
			}
			case SEMI:
			case VARARGS:
			case LITERAL_asm:
			case RCURLY:
			case RPAREN:
			case COMMA:
			case COLON:
			case ASSIGN:
			case LITERAL___attribute:
			case RBRACKET:
			case DIV_ASSIGN:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case MOD_ASSIGN:
			case RSHIFT_ASSIGN:
			case LSHIFT_ASSIGN:
			case BAND_ASSIGN:
			case BOR_ASSIGN:
			case BXOR_ASSIGN:
			case RKERNEL_LAUNCH:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_75);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final AssignmentOperator  assignOperator() throws RecognitionException, TokenStreamException {
		AssignmentOperator code;
		
		code = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ASSIGN:
			{
				match(ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.NORMAL;
				}
				break;
			}
			case DIV_ASSIGN:
			{
				match(DIV_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.DIVIDE;
				}
				break;
			}
			case PLUS_ASSIGN:
			{
				match(PLUS_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.ADD;
				}
				break;
			}
			case MINUS_ASSIGN:
			{
				match(MINUS_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.SUBTRACT;
				}
				break;
			}
			case STAR_ASSIGN:
			{
				match(STAR_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.MULTIPLY;
				}
				break;
			}
			case MOD_ASSIGN:
			{
				match(MOD_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.MODULUS;
				}
				break;
			}
			case RSHIFT_ASSIGN:
			{
				match(RSHIFT_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.SHIFT_RIGHT;
				}
				break;
			}
			case LSHIFT_ASSIGN:
			{
				match(LSHIFT_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.SHIFT_LEFT;
				}
				break;
			}
			case BAND_ASSIGN:
			{
				match(BAND_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.BITWISE_AND;
				}
				break;
			}
			case BOR_ASSIGN:
			{
				match(BOR_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.BITWISE_INCLUSIVE_OR;
				}
				break;
			}
			case BXOR_ASSIGN:
			{
				match(BXOR_ASSIGN);
				if ( inputState.guessing==0 ) {
					code = AssignmentOperator.BITWISE_EXCLUSIVE_OR;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final Expression  logicalOrExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=logicalAndExpr();
			{
			_loop222:
			do {
				if ((LA(1)==LOR)) {
					match(LOR);
					expr1=logicalAndExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,BinaryOperator.LOGICAL_OR,expr1);
					}
				}
				else {
					break _loop222;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_76);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  logicalAndExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=inclusiveOrExpr();
			{
			_loop225:
			do {
				if ((LA(1)==LAND)) {
					match(LAND);
					expr1=inclusiveOrExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,BinaryOperator.LOGICAL_AND,expr1);
					}
				}
				else {
					break _loop225;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_77);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  inclusiveOrExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=exclusiveOrExpr();
			{
			_loop228:
			do {
				if ((LA(1)==BOR)) {
					match(BOR);
					expr1=exclusiveOrExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,BinaryOperator.BITWISE_INCLUSIVE_OR,expr1);
					}
				}
				else {
					break _loop228;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_78);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  exclusiveOrExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=bitAndExpr();
			{
			_loop231:
			do {
				if ((LA(1)==BXOR)) {
					match(BXOR);
					expr1=bitAndExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,BinaryOperator.BITWISE_EXCLUSIVE_OR,expr1);
					}
				}
				else {
					break _loop231;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_79);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  bitAndExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=equalityExpr();
			{
			_loop234:
			do {
				if ((LA(1)==BAND)) {
					match(BAND);
					expr1=equalityExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,BinaryOperator.BITWISE_AND,expr1);
					}
				}
				else {
					break _loop234;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_80);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  equalityExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=relationalExpr();
			{
			_loop238:
			do {
				if ((LA(1)==EQUAL||LA(1)==NOT_EQUAL)) {
					{
					switch ( LA(1)) {
					case EQUAL:
					{
						match(EQUAL);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.COMPARE_EQ;
						}
						break;
					}
					case NOT_EQUAL:
					{
						match(NOT_EQUAL);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.COMPARE_NE;
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr1=relationalExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,code,expr1);
					}
				}
				else {
					break _loop238;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_81);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  relationalExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=shiftExpr();
			{
			_loop242:
			do {
				if (((LA(1) >= LT && LA(1) <= GTE))) {
					{
					switch ( LA(1)) {
					case LT:
					{
						match(LT);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.COMPARE_LT;
						}
						break;
					}
					case LTE:
					{
						match(LTE);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.COMPARE_LE;
						}
						break;
					}
					case GT:
					{
						match(GT);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.COMPARE_GT;
						}
						break;
					}
					case GTE:
					{
						match(GTE);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.COMPARE_GE;
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr1=shiftExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,code,expr1);
					}
				}
				else {
					break _loop242;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_82);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  shiftExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=additiveExpr();
			{
			_loop246:
			do {
				if ((LA(1)==LSHIFT||LA(1)==RSHIFT)) {
					{
					switch ( LA(1)) {
					case LSHIFT:
					{
						match(LSHIFT);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.SHIFT_LEFT;
						}
						break;
					}
					case RSHIFT:
					{
						match(RSHIFT);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.SHIFT_RIGHT;
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr1=additiveExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,code,expr1);
					}
				}
				else {
					break _loop246;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_83);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  additiveExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=multExpr();
			{
			_loop250:
			do {
				if ((LA(1)==PLUS||LA(1)==MINUS)) {
					{
					switch ( LA(1)) {
					case PLUS:
					{
						match(PLUS);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.ADD;
						}
						break;
					}
					case MINUS:
					{
						match(MINUS);
						if ( inputState.guessing==0 ) {
							code=BinaryOperator.SUBTRACT;
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr1=multExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,code,expr1);
					}
				}
				else {
					break _loop250;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_84);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  multExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
						Expression expr1, expr2; ret_expr=null;
						BinaryOperator code = null;
					
		
		try {      // for error handling
			ret_expr=castExpr();
			{
			_loop254:
			do {
				if ((_tokenSet_85.member(LA(1)))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						match(STAR);
						if ( inputState.guessing==0 ) {
							code = BinaryOperator.MULTIPLY;
						}
						break;
					}
					case DIV:
					{
						match(DIV);
						if ( inputState.guessing==0 ) {
							code=BinaryOperator.DIVIDE;
						}
						break;
					}
					case MOD:
					{
						match(MOD);
						if ( inputState.guessing==0 ) {
							code=BinaryOperator.MODULUS;
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					expr1=castExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new BinaryExpression(ret_expr,code,expr1);
					}
				}
				else {
					break _loop254;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_86);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  castExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
							ret_expr = null;
							Expression expr1=null;
							List tname=null;
						
		
		try {      // for error handling
			boolean synPredMatched272 = false;
			if (((LA(1)==LPAREN) && (_tokenSet_22.member(LA(2))))) {
				int _m272 = mark();
				synPredMatched272 = true;
				inputState.guessing++;
				try {
					{
					match(LPAREN);
					typeName();
					match(RPAREN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched272 = false;
				}
				rewind(_m272);
				inputState.guessing--;
			}
			if ( synPredMatched272 ) {
				match(LPAREN);
				tname=typeName();
				match(RPAREN);
				{
				switch ( LA(1)) {
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					expr1=castExpr();
					if ( inputState.guessing==0 ) {
						
								//System.err.println("Typecast");
								ret_expr = new Typecast(tname,expr1);
							
					}
					break;
				}
				case LCURLY:
				{
					lcurlyInitializer();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
			}
			else if ((_tokenSet_15.member(LA(1))) && (_tokenSet_87.member(LA(2)))) {
				ret_expr=unaryExpr();
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  postfixExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
							ret_expr=null;
							Expression expr1=null;
						
		
		try {      // for error handling
			expr1=primaryExpr();
			if ( inputState.guessing==0 ) {
				ret_expr = expr1;
			}
			{
			switch ( LA(1)) {
			case LPAREN:
			case LBRACKET:
			case DOT:
			case PTR:
			case INC:
			case DEC:
			case LKERNEL_LAUNCH:
			{
				ret_expr=postfixSuffix(expr1);
				break;
			}
			case SEMI:
			case VARARGS:
			case LITERAL_asm:
			case RCURLY:
			case RPAREN:
			case COMMA:
			case COLON:
			case ASSIGN:
			case LITERAL___attribute:
			case STAR:
			case RBRACKET:
			case DIV_ASSIGN:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case MOD_ASSIGN:
			case RSHIFT_ASSIGN:
			case LSHIFT_ASSIGN:
			case BAND_ASSIGN:
			case BOR_ASSIGN:
			case BXOR_ASSIGN:
			case LOR:
			case LAND:
			case BOR:
			case BXOR:
			case BAND:
			case EQUAL:
			case NOT_EQUAL:
			case LT:
			case LTE:
			case GT:
			case GTE:
			case LSHIFT:
			case RSHIFT:
			case PLUS:
			case MINUS:
			case DIV:
			case MOD:
			case RKERNEL_LAUNCH:
			case QUESTION:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final Expression  primaryExpr() throws RecognitionException, TokenStreamException {
		Expression p;
		
		Token  prim_id = null;
		Token  prim_num = null;
		
				Expression expr1=null;
				CompoundStatement cstmt = null;
				p=null;
				String name = null;
				//TNode temp;
				
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				prim_id = LT(1);
				match(ID);
				if ( inputState.guessing==0 ) {
					
								name = prim_id.getText();
										//Declaration d = Tools.findSymbol(curr_cstmt,new Identifier(name));
						       			/*
						       			if(d == null)
											System.err.println("Warning unregistered symbol: "+name);
										*/
										p=new Identifier(name);
							
				}
				break;
			}
			case Number:
			{
				prim_num = LT(1);
				match(Number);
				if ( inputState.guessing==0 ) {
					
							 	name = prim_num.getText();
							 	boolean handled = false;
							 	int i = 0;
							 	int radix = 10;
							 	double d = 0;
							 	Integer i2 = null;
							 	Long in = null;
									
									//JAS: July 2008
									//Having issues with preserving the signed/unsigned 
									// properties of immediates.  Current IR printing 
									// assumes all numbers are signed, and we need input from 
									// the parser to correct that at all.  
									// While we're at it, we may as well preserve the 
									// input format for easier correlation.
									
							 	name = name.toUpperCase();
							 		name = name.replaceAll("L","");
							 		name = name.replaceAll("U","");
							 	 	name= name.replaceAll("I","");
					
							 	if(name.startsWith("0X") == false){
							 		name.replaceAll("F","");
							 	}
							  	try{
					
							 		i2 = Integer.decode(name);
										//JAS, need to use the actual string here
							 		p=new IntegerLiteral(name);
							 		handled = true;
							 	}catch(NumberFormatException e){
							 		;
							 	}
							 	if(handled == false)
							 	try{
					
					
							 		in = Long.decode(name);
										//JAS, again, want the output format to match
							 		p=new IntegerLiteral(name);
							 		handled = true;
							 	}
							 	catch(NumberFormatException e){
							 		;
					
							 	}
							 	if(handled == false)
							 	try{d = Double.parseDouble(name);
									  if(name.lastIndexOf("F") != -1) {
									    p = new FloatLiteral(d, true);
									  } else {
									    p = new FloatLiteral(d, false);
							          }
									  handled = true;
					
							 	}catch(NumberFormatException e){
					
							 		p=new Identifier(name);
					
							 		Tools.printlnStatus("Strange number "+name,0);
							 		//System.exit(1);
							 	}
					
							
				}
				break;
			}
			case CharLiteral:
			{
				name=charConst();
				if ( inputState.guessing==0 ) {
					
								//Scanner scan = new Scanner(name);
								//Tools.printlnStatus("CharLiteral "+name.charAt(1)+" "+name.length(),0);
								if(name.length()==3)
									p = new CharLiteral(name.charAt(1));
								// escape sequence is not handled at this point
								else{
					/*
									char c = name.charAt(2);
									switch(c){
										case 'a':
										p =new CharLiteral('\7');
										break;
										case 'b':
										p =new CharLiteral('\b');
										break;
										case 'f':
										p =new CharLiteral('\f');
										break;
										case 'n':
										p =new CharLiteral('\n');
										break;
										case 'r':
										p =new CharLiteral('\r');
										break;
										case 't':
										p =new CharLiteral('\t');
										break;
										case 'v':
										p =new CharLiteral('\14');
										break;
										case '\\':
										p =new CharLiteral('\\');
										break;
										case '?':
										p =new CharLiteral('\77');
										break;
										case '\'':
										p =new CharLiteral('\'');
										break;
										case '\"':
										p =new CharLiteral('\"');
										break;
										case 'x':
											c = (char)Integer.parseInt(name.substring(3,name.length()-1),16);
											p = new CharLiteral(c);
										break;
										default:
											if(c <= '7' && c >= '0'){
												c = (char)Integer.parseInt(name.substring(2,name.length()-1),8);
										 		p = new CharLiteral(c);
					
											}
											else{
					
												Tools.printlnStatus("Unrecognized Escape Sequence "+name,0);
												p = new Identifier(name);
											}
										break;
									}
					*/        				p = new EscapeLiteral(name);
								}
					
					
							
				}
				break;
			}
			case StringLiteral:
			{
				name=stringConst();
				if ( inputState.guessing==0 ) {
					
								//name = name.replaceAll("\"","");
								p=new StringLiteral(name);
								((StringLiteral)p).stripQuotes();
							
				}
				break;
			}
			default:
				boolean synPredMatched319 = false;
				if (((LA(1)==LPAREN) && (LA(2)==LCURLY))) {
					int _m319 = mark();
					synPredMatched319 = true;
					inputState.guessing++;
					try {
						{
						match(LPAREN);
						match(LCURLY);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched319 = false;
					}
					rewind(_m319);
					inputState.guessing--;
				}
				if ( synPredMatched319 ) {
					match(LPAREN);
					cstmt=compoundStatement();
					match(RPAREN);
					if ( inputState.guessing==0 ) {
						
										Tools.printlnStatus("[DEBUG] Warning: CompoundStatement Expression !",0);
										ByteArrayOutputStream bs = new ByteArrayOutputStream();
										cstmt.print(bs);
										p = new StringLiteral(bs.toString());
									
					}
				}
				else if ((LA(1)==LPAREN) && (_tokenSet_15.member(LA(2)))) {
					match(LPAREN);
					expr1=expr();
					match(RPAREN);
					if ( inputState.guessing==0 ) {
						
											p=expr1;
										
					}
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		return p;
	}
	
	public final Expression  postfixSuffix(
		Expression expr1
	) throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		Token  ptr_id = null;
		Token  dot_id = null;
		
					Expression expr2=null;
					//boolean builtin=false;
					SymbolTable saveSymtab = null;
					String s;
							ret_expr = expr1;
							List args = null;
						
		
		try {      // for error handling
			{
			int _cnt261=0;
			_loop261:
			do {
				switch ( LA(1)) {
				case PTR:
				{
					match(PTR);
					ptr_id = LT(1);
					match(ID);
					if ( inputState.guessing==0 ) {
						
							/*
							ret_expr =
							new BinaryExpression(ret_expr,BinaryOperator.POINTER_ACCESS,new Identifier(ptr_id.getText()));
							*/
							ret_expr = new AccessExpression(ret_expr,AccessOperator.POINTER_ACCESS,new Identifier(ptr_id.getText()));
						
					}
					break;
				}
				case DOT:
				{
					match(DOT);
					dot_id = LT(1);
					match(ID);
					if ( inputState.guessing==0 ) {
						
							/*
							ret_expr =
							new BinaryExpression(ret_expr,BinaryOperator.MEMBER_ACCESS,new Identifier(dot_id.getText()));
							*/
							ret_expr =
							new AccessExpression(ret_expr,AccessOperator.MEMBER_ACCESS,new Identifier(dot_id.getText()));
						
					}
					break;
				}
				case LPAREN:
				case LKERNEL_LAUNCH:
				{
					args=functionCall();
					if ( inputState.guessing==0 ) {
						
							if(args == null)
								ret_expr = new FunctionCall(ret_expr);
							else
								ret_expr = new FunctionCall(ret_expr,args);
						
					}
					break;
				}
				case LBRACKET:
				{
					match(LBRACKET);
					expr2=expr();
					match(RBRACKET);
					if ( inputState.guessing==0 ) {
						
							if(ret_expr instanceof ArrayAccess){
								ArrayAccess aacc = (ArrayAccess)ret_expr;
								int dim = aacc.getNumIndices();
								int n = 0;
								ArrayList alist = new ArrayList();
								for(n = 0;n < dim; n++){
						
									alist.add(aacc.getIndex(n));
												}
												alist.add(expr2);
								aacc.setIndices(alist);
						
							}
							else
												ret_expr = new ArrayAccess(ret_expr,expr2);
										
					}
					break;
				}
				case INC:
				{
					match(INC);
					if ( inputState.guessing==0 ) {
						ret_expr = new UnaryExpression(UnaryOperator.POST_INCREMENT,ret_expr);
					}
					break;
				}
				case DEC:
				{
					match(DEC);
					if ( inputState.guessing==0 ) {
						ret_expr = new UnaryExpression(UnaryOperator.POST_DECREMENT,ret_expr);
					}
					break;
				}
				default:
				{
					if ( _cnt261>=1 ) { break _loop261; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				}
				_cnt261++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
/**
 * John A. Stratton: February 2008
 * Allow CUDA kernel launches, but automatically convert to 
 * a normal function call with extra parameters.
 * For some special implementations, the dim3 structure needs to be unpacked.
 */
	public final List  functionCall() throws RecognitionException, TokenStreamException {
		List args;
		
		
							args=null;
							List args2=null;
							List args3=null;
						
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			{
				match(LPAREN);
				{
				switch ( LA(1)) {
				case LITERAL_asm:
				case LPAREN:
				case ID:
				case STAR:
				case BAND:
				case PLUS:
				case MINUS:
				case INC:
				case DEC:
				case LITERAL_sizeof:
				case LITERAL___alignof:
				case BNOT:
				case LNOT:
				case LITERAL___real:
				case LITERAL___imag:
				case Number:
				case CharLiteral:
				case StringLiteral:
				{
					args=argExprList();
					break;
				}
				case RPAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RPAREN);
				break;
			}
			case LKERNEL_LAUNCH:
			{
				{
				match(LKERNEL_LAUNCH);
				args2=argExprList();
				match(RKERNEL_LAUNCH);
				}
				if ( inputState.guessing==0 ) {
					
							     args= new ArrayList();
							     if(Driver.getOptionValue("Xpilot") == null) {
							       args.add(args2.get(0));
							       args.add(args2.get(1));
							     }
							     else{
							       args.add(new AccessExpression(((Expression)args2.get(0)), 
							       				AccessOperator.MEMBER_ACCESS,
											new Identifier(new String("x"))));
							       args.add(new AccessExpression(((Expression)args2.get(0)), 
							       				AccessOperator.MEMBER_ACCESS,
											new Identifier(new String("y"))));
							       args.add(new AccessExpression(((Expression)args2.get(1)), 
							       				AccessOperator.MEMBER_ACCESS,
											new Identifier(new String("x"))));
							       args.add(new AccessExpression(((Expression)args2.get(1)), 
							       				AccessOperator.MEMBER_ACCESS,
											new Identifier(new String("y"))));
							       args.add(new AccessExpression(((Expression)args2.get(1)), 
							       				AccessOperator.MEMBER_ACCESS,
											new Identifier(new String("z"))));
					}
							
				}
				{
				args3=functionCall();
				}
				if ( inputState.guessing==0 ) {
					
							  args.addAll(0,args3);
							
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		return args;
	}
	
	public final List  argExprList() throws RecognitionException, TokenStreamException {
		List eList;
		
		
						Expression expr1 = null;
						eList=new ArrayList();
						Declaration pdecl = null;
					
		
		try {      // for error handling
			expr1=assignExpr();
			if ( inputState.guessing==0 ) {
				eList.add(expr1);
			}
			{
			_loop323:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					{
					if ((_tokenSet_15.member(LA(1))) && (_tokenSet_88.member(LA(2)))) {
						expr1=assignExpr();
						if ( inputState.guessing==0 ) {
							eList.add(expr1);
						}
					}
					else if ((_tokenSet_1.member(LA(1))) && (_tokenSet_89.member(LA(2)))) {
						pdecl=parameterDeclaration();
						if ( inputState.guessing==0 ) {
							
							
									 	eList.add(/*new StringLiteral(pdecl.toString())*/pdecl);
									
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
				else {
					break _loop323;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_90);
			} else {
			  throw ex;
			}
		}
		return eList;
	}
	
	public final Expression  unaryExpr() throws RecognitionException, TokenStreamException {
		Expression ret_expr;
		
		
							Expression expr1=null;
							UnaryOperator code;
							ret_expr = null;
							List tname = null;
						
		
		try {      // for error handling
			switch ( LA(1)) {
			case LPAREN:
			case ID:
			case Number:
			case CharLiteral:
			case StringLiteral:
			{
				ret_expr=postfixExpr();
				break;
			}
			case INC:
			{
				match(INC);
				expr1=castExpr();
				if ( inputState.guessing==0 ) {
					ret_expr = new UnaryExpression(UnaryOperator.PRE_INCREMENT, expr1);
				}
				break;
			}
			case DEC:
			{
				match(DEC);
				expr1=castExpr();
				if ( inputState.guessing==0 ) {
					ret_expr = new UnaryExpression(UnaryOperator.PRE_DECREMENT, expr1);
				}
				break;
			}
			case STAR:
			case BAND:
			case PLUS:
			case MINUS:
			case BNOT:
			case LNOT:
			case LITERAL___real:
			case LITERAL___imag:
			{
				code=unaryOperator();
				expr1=castExpr();
				if ( inputState.guessing==0 ) {
					
										ret_expr = new UnaryExpression(code, expr1);
									
				}
				break;
			}
			case LITERAL_sizeof:
			{
				match(LITERAL_sizeof);
				{
				boolean synPredMatched297 = false;
				if (((LA(1)==LPAREN) && (_tokenSet_22.member(LA(2))))) {
					int _m297 = mark();
					synPredMatched297 = true;
					inputState.guessing++;
					try {
						{
						match(LPAREN);
						typeName();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched297 = false;
					}
					rewind(_m297);
					inputState.guessing--;
				}
				if ( synPredMatched297 ) {
					match(LPAREN);
					tname=typeName();
					match(RPAREN);
					if ( inputState.guessing==0 ) {
						ret_expr = new SizeofExpression(tname);
					}
				}
				else if ((_tokenSet_15.member(LA(1))) && (_tokenSet_87.member(LA(2)))) {
					expr1=unaryExpr();
					if ( inputState.guessing==0 ) {
						ret_expr = new SizeofExpression(expr1);
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case LITERAL___alignof:
			{
				match(LITERAL___alignof);
				{
				boolean synPredMatched300 = false;
				if (((LA(1)==LPAREN) && (_tokenSet_22.member(LA(2))))) {
					int _m300 = mark();
					synPredMatched300 = true;
					inputState.guessing++;
					try {
						{
						match(LPAREN);
						typeName();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched300 = false;
					}
					rewind(_m300);
					inputState.guessing--;
				}
				if ( synPredMatched300 ) {
					match(LPAREN);
					tname=typeName();
					match(RPAREN);
				}
				else if ((_tokenSet_15.member(LA(1))) && (_tokenSet_87.member(LA(2)))) {
					expr1=unaryExpr();
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
				break;
			}
			case LITERAL_asm:
			{
				gnuAsmExpr();
				if ( inputState.guessing==0 ) {
					ret_expr = new Identifier("asm");
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		return ret_expr;
	}
	
	public final UnaryOperator  unaryOperator() throws RecognitionException, TokenStreamException {
		UnaryOperator code;
		
		code = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case BAND:
			{
				match(BAND);
				if ( inputState.guessing==0 ) {
					code = UnaryOperator.ADDRESS_OF;
				}
				break;
			}
			case STAR:
			{
				match(STAR);
				if ( inputState.guessing==0 ) {
					code = UnaryOperator.DEREFERENCE;
				}
				break;
			}
			case PLUS:
			{
				match(PLUS);
				if ( inputState.guessing==0 ) {
					code = UnaryOperator.PLUS;
				}
				break;
			}
			case MINUS:
			{
				match(MINUS);
				if ( inputState.guessing==0 ) {
					code = UnaryOperator.MINUS;
				}
				break;
			}
			case BNOT:
			{
				match(BNOT);
				if ( inputState.guessing==0 ) {
					code = UnaryOperator.BITWISE_COMPLEMENT;
				}
				break;
			}
			case LNOT:
			{
				match(LNOT);
				if ( inputState.guessing==0 ) {
					code = UnaryOperator.LOGICAL_NEGATION;
				}
				break;
			}
			case LITERAL___real:
			{
				match(LITERAL___real);
				if ( inputState.guessing==0 ) {
					code = null;
				}
				break;
			}
			case LITERAL___imag:
			{
				match(LITERAL___imag);
				if ( inputState.guessing==0 ) {
					code = null;
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void gnuAsmExpr() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LITERAL_asm);
			{
			switch ( LA(1)) {
			case LITERAL_volatile:
			{
				match(LITERAL_volatile);
				break;
			}
			case LPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LPAREN);
			stringConst();
			{
			if ((LA(1)==COLON) && (_tokenSet_91.member(LA(2)))) {
				match(COLON);
				{
				switch ( LA(1)) {
				case StringLiteral:
				{
					strOptExprPair();
					{
					_loop307:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							strOptExprPair();
						}
						else {
							break _loop307;
						}
						
					} while (true);
					}
					break;
				}
				case RPAREN:
				case COLON:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				if ((LA(1)==COLON) && (_tokenSet_91.member(LA(2)))) {
					match(COLON);
					{
					switch ( LA(1)) {
					case StringLiteral:
					{
						strOptExprPair();
						{
						_loop311:
						do {
							if ((LA(1)==COMMA)) {
								match(COMMA);
								strOptExprPair();
							}
							else {
								break _loop311;
							}
							
						} while (true);
						}
						break;
					}
					case RPAREN:
					case COLON:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else if ((LA(1)==RPAREN||LA(1)==COLON) && (_tokenSet_92.member(LA(2)))) {
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else if ((LA(1)==RPAREN||LA(1)==COLON) && (_tokenSet_92.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case COLON:
			{
				match(COLON);
				stringConst();
				{
				_loop314:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						stringConst();
					}
					else {
						break _loop314;
					}
					
				} while (true);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void strOptExprPair() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			stringConst();
			{
			switch ( LA(1)) {
			case LPAREN:
			{
				match(LPAREN);
				expr();
				match(RPAREN);
				break;
			}
			case RPAREN:
			case COMMA:
			case COLON:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_93);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final String  charConst() throws RecognitionException, TokenStreamException {
		String name;
		
		Token  cl = null;
		name = null;
		
		try {      // for error handling
			cl = LT(1);
			match(CharLiteral);
			if ( inputState.guessing==0 ) {
				name=cl.getText();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		return name;
	}
	
	protected final void intConst() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case IntOctalConst:
			{
				match(IntOctalConst);
				break;
			}
			case LongOctalConst:
			{
				match(LongOctalConst);
				break;
			}
			case UnsignedOctalConst:
			{
				match(UnsignedOctalConst);
				break;
			}
			case IntIntConst:
			{
				match(IntIntConst);
				break;
			}
			case LongIntConst:
			{
				match(LongIntConst);
				break;
			}
			case UnsignedIntConst:
			{
				match(UnsignedIntConst);
				break;
			}
			case IntHexConst:
			{
				match(IntHexConst);
				break;
			}
			case LongHexConst:
			{
				match(LongHexConst);
				break;
			}
			case UnsignedHexConst:
			{
				match(UnsignedHexConst);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	protected final void floatConst() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case FloatDoubleConst:
			{
				match(FloatDoubleConst);
				break;
			}
			case DoubleDoubleConst:
			{
				match(DoubleDoubleConst);
				break;
			}
			case LongDoubleConst:
			{
				match(LongDoubleConst);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void dummy() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case NTypedefName:
			{
				match(NTypedefName);
				break;
			}
			case NInitDecl:
			{
				match(NInitDecl);
				break;
			}
			case NDeclarator:
			{
				match(NDeclarator);
				break;
			}
			case NStructDeclarator:
			{
				match(NStructDeclarator);
				break;
			}
			case NDeclaration:
			{
				match(NDeclaration);
				break;
			}
			case NCast:
			{
				match(NCast);
				break;
			}
			case NPointerGroup:
			{
				match(NPointerGroup);
				break;
			}
			case NExpressionGroup:
			{
				match(NExpressionGroup);
				break;
			}
			case NFunctionCallArgs:
			{
				match(NFunctionCallArgs);
				break;
			}
			case NNonemptyAbstractDeclarator:
			{
				match(NNonemptyAbstractDeclarator);
				break;
			}
			case NInitializer:
			{
				match(NInitializer);
				break;
			}
			case NStatementExpr:
			{
				match(NStatementExpr);
				break;
			}
			case NEmptyExpression:
			{
				match(NEmptyExpression);
				break;
			}
			case NParameterTypeList:
			{
				match(NParameterTypeList);
				break;
			}
			case NFunctionDef:
			{
				match(NFunctionDef);
				break;
			}
			case NCompoundStatement:
			{
				match(NCompoundStatement);
				break;
			}
			case NParameterDeclaration:
			{
				match(NParameterDeclaration);
				break;
			}
			case NCommaExpr:
			{
				match(NCommaExpr);
				break;
			}
			case NUnaryExpr:
			{
				match(NUnaryExpr);
				break;
			}
			case NLabel:
			{
				match(NLabel);
				break;
			}
			case NPostfixExpr:
			{
				match(NPostfixExpr);
				break;
			}
			case NRangeExpr:
			{
				match(NRangeExpr);
				break;
			}
			case NStringSeq:
			{
				match(NStringSeq);
				break;
			}
			case NInitializerElementLabel:
			{
				match(NInitializerElementLabel);
				break;
			}
			case NLcurlyInitializer:
			{
				match(NLcurlyInitializer);
				break;
			}
			case NAsmAttribute:
			{
				match(NAsmAttribute);
				break;
			}
			case NGnuAsmExpr:
			{
				match(NGnuAsmExpr);
				break;
			}
			case NTypeMissing:
			{
				match(NTypeMissing);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"a line directive",
		"\"typedef\"",
		"SEMI",
		"VARARGS",
		"LCURLY",
		"\"asm\"",
		"\"volatile\"",
		"RCURLY",
		"\"struct\"",
		"\"union\"",
		"\"enum\"",
		"\"auto\"",
		"\"register\"",
		"\"extern\"",
		"\"static\"",
		"\"inline\"",
		"\"__global__\"",
		"\"__device__\"",
		"\"__host__\"",
		"\"__kernel\"",
		"\"__global\"",
		"\"__local\"",
		"\"const\"",
		"\"__shared__\"",
		"\"__constant__\"",
		"\"void\"",
		"\"char\"",
		"\"short\"",
		"\"int\"",
		"\"long\"",
		"\"float\"",
		"\"double\"",
		"\"signed\"",
		"\"unsigned\"",
		"\"_Bool\"",
		"\"_Complex\"",
		"\"_Imaginary\"",
		"\"bool\"",
		"\"typeof\"",
		"LPAREN",
		"RPAREN",
		"\"__complex\"",
		"ID",
		"COMMA",
		"COLON",
		"ASSIGN",
		"LITERAL___attribute",
		"STAR",
		"LBRACKET",
		"RBRACKET",
		"DOT",
		"\"__label__\"",
		"\"while\"",
		"\"do\"",
		"\"for\"",
		"\"goto\"",
		"\"continue\"",
		"\"break\"",
		"\"return\"",
		"\"case\"",
		"\"default\"",
		"\"if\"",
		"\"else\"",
		"\"switch\"",
		"DIV_ASSIGN",
		"PLUS_ASSIGN",
		"MINUS_ASSIGN",
		"STAR_ASSIGN",
		"MOD_ASSIGN",
		"RSHIFT_ASSIGN",
		"LSHIFT_ASSIGN",
		"BAND_ASSIGN",
		"BOR_ASSIGN",
		"BXOR_ASSIGN",
		"LOR",
		"LAND",
		"BOR",
		"BXOR",
		"BAND",
		"EQUAL",
		"NOT_EQUAL",
		"LT",
		"LTE",
		"GT",
		"GTE",
		"LSHIFT",
		"RSHIFT",
		"PLUS",
		"MINUS",
		"DIV",
		"MOD",
		"PTR",
		"INC",
		"DEC",
		"LKERNEL_LAUNCH",
		"RKERNEL_LAUNCH",
		"QUESTION",
		"\"sizeof\"",
		"\"__alignof\"",
		"BNOT",
		"LNOT",
		"\"__real\"",
		"\"__imag\"",
		"Number",
		"CharLiteral",
		"StringLiteral",
		"IntOctalConst",
		"LongOctalConst",
		"UnsignedOctalConst",
		"IntIntConst",
		"LongIntConst",
		"UnsignedIntConst",
		"IntHexConst",
		"LongHexConst",
		"UnsignedHexConst",
		"FloatDoubleConst",
		"DoubleDoubleConst",
		"LongDoubleConst",
		"NTypedefName",
		"NInitDecl",
		"NDeclarator",
		"NStructDeclarator",
		"NDeclaration",
		"NCast",
		"NPointerGroup",
		"NExpressionGroup",
		"NFunctionCallArgs",
		"NNonemptyAbstractDeclarator",
		"NInitializer",
		"NStatementExpr",
		"NEmptyExpression",
		"NParameterTypeList",
		"NFunctionDef",
		"NCompoundStatement",
		"NParameterDeclaration",
		"NCommaExpr",
		"NUnaryExpr",
		"NLabel",
		"NPostfixExpr",
		"NRangeExpr",
		"NStringSeq",
		"NInitializerElementLabel",
		"NLcurlyInitializer",
		"NAsmAttribute",
		"NGnuAsmExpr",
		"NTypeMissing",
		"\"__extension__\"",
		"Vocabulary",
		"Whitespace",
		"Comment",
		"CPPComment",
		"Space",
		"LineDirective",
		"BadStringLiteral",
		"Escape",
		"IntSuffix",
		"NumberSuffix",
		"Digit",
		"Exponent",
		"IDMEAT",
		"WideCharLiteral",
		"WideStringLiteral"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 1240249116128800L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 3500845022836576L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 3500845022737920L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 8004444650207200L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 3456864557728256L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 8945627075446336L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 3500845022836338L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { -32527951996125198L, 70244593041419L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 114349209187328L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 3500845022738176L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 471860224L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 114348672446464L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 2243003720660960L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 64L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 2330964650885632L, 70244593041408L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 9447003905853504L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 67076128L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 8162774324606560L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 8162774324606816L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 8118793859498560L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 8444249301317216L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 114349144306688L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 6896136864429312L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 25614222815688448L, 70334384439280L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 9007199254738528L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 17592186044416L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { 70368744177920L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { 2048L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { 114349144308736L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { 8444249236338496L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { 8400268836209216L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 3879077022794304L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { 140737488355392L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { 8382677122025024L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { 1548112371909184L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { 3923057422925376L, 70244593041408L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { 10555311626652352L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { 140737488357376L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { 35914447809678016L, 137438953456L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { -167125767421968L, -1L, 274877906943L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { 158329674399744L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { 24848962787738368L, 70244593041408L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { 25878105606356800L, 70334384439280L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { 22588366881030144L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { 2612439627596288L, 70244593041408L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { 2330964650885888L, 70244593041408L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { 25596630629646144L, 70334384439280L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { 24848962787740416L, 70244593041408L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = { 140737488357440L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 5866994045813312L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { 24892943187867520L, 70334384422912L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { 33900142442608384L, 70334384422912L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = { 9447003905853504L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = { 13387653579803328L, 105226698736L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { 9288674231451648L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = { 8162774324606816L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = { 228698418577408L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = { 6755399441053664L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { 8118794331358784L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = { 6764195534077952L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = { 17169973579347552L, 70278952779776L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { 158329674399808L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = { 37269046135092768L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { 3500845022770688L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = { 8004444650207008L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = { -69726629387041984L, 70244593041419L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = { -9024791440785568L, 70334384439291L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = { -32510359810080910L, 70244593041423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = { -32527951996125344L, 70244593041419L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = { 25174418164578048L, 70334384422912L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = { -9024791440785566L, 70334384439295L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	private static final long[] mk_tokenSet_72() {
		long[] data = { -32527951996125342L, 70244593041423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	private static final long[] mk_tokenSet_73() {
		long[] data = { -9007199254741134L, 70334384439295L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_73 = new BitSet(mk_tokenSet_73());
	private static final long[] mk_tokenSet_74() {
		long[] data = { 25596630629644096L, 70334384439280L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_74 = new BitSet(mk_tokenSet_74());
	private static final long[] mk_tokenSet_75() {
		long[] data = { 11135853766118080L, 34359754736L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_75 = new BitSet(mk_tokenSet_75());
	private static final long[] mk_tokenSet_76() {
		long[] data = { 11135853766118080L, 103079231472L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_76 = new BitSet(mk_tokenSet_76());
	private static final long[] mk_tokenSet_77() {
		long[] data = { 11135853766118080L, 103079247856L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_77 = new BitSet(mk_tokenSet_77());
	private static final long[] mk_tokenSet_78() {
		long[] data = { 11135853766118080L, 103079280624L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_78 = new BitSet(mk_tokenSet_78());
	private static final long[] mk_tokenSet_79() {
		long[] data = { 11135853766118080L, 103079346160L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_79 = new BitSet(mk_tokenSet_79());
	private static final long[] mk_tokenSet_80() {
		long[] data = { 11135853766118080L, 103079477232L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_80 = new BitSet(mk_tokenSet_80());
	private static final long[] mk_tokenSet_81() {
		long[] data = { 11135853766118080L, 103079739376L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_81 = new BitSet(mk_tokenSet_81());
	private static final long[] mk_tokenSet_82() {
		long[] data = { 11135853766118080L, 103081312240L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_82 = new BitSet(mk_tokenSet_82());
	private static final long[] mk_tokenSet_83() {
		long[] data = { 11135853766118080L, 103112769520L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_83 = new BitSet(mk_tokenSet_83());
	private static final long[] mk_tokenSet_84() {
		long[] data = { 11135853766118080L, 103213432816L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_84 = new BitSet(mk_tokenSet_84());
	private static final long[] mk_tokenSet_85() {
		long[] data = { 2251799813685248L, 1610612736L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_85 = new BitSet(mk_tokenSet_85());
	private static final long[] mk_tokenSet_86() {
		long[] data = { 11135853766118080L, 103616086000L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_86 = new BitSet(mk_tokenSet_86());
	private static final long[] mk_tokenSet_87() {
		long[] data = { 35984816553856960L, 70368744177648L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_87 = new BitSet(mk_tokenSet_87());
	private static final long[] mk_tokenSet_88() {
		long[] data = { 25614222815688448L, 70368744177648L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_88 = new BitSet(mk_tokenSet_88());
	private static final long[] mk_tokenSet_89() {
		long[] data = { 8162774324606752L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_89 = new BitSet(mk_tokenSet_89());
	private static final long[] mk_tokenSet_90() {
		long[] data = { 17592186044416L, 34359738368L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_90 = new BitSet(mk_tokenSet_90());
	private static final long[] mk_tokenSet_91() {
		long[] data = { 299067162755072L, 35184372088832L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_91 = new BitSet(mk_tokenSet_91());
	private static final long[] mk_tokenSet_92() {
		long[] data = { 13387653579803328L, 35289598787568L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_92 = new BitSet(mk_tokenSet_92());
	private static final long[] mk_tokenSet_93() {
		long[] data = { 439804651110400L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_93 = new BitSet(mk_tokenSet_93());
	
	}
