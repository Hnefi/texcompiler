package ece.ncsu.edu.gpucompiler;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cetus.hir.BreadthFirstIterator;
import cetus.hir.CompoundStatement;
import cetus.hir.Declaration;
import cetus.hir.Declarator;
import cetus.hir.FunctionCall;
import cetus.hir.IDExpression;
import cetus.hir.Identifier;
import cetus.hir.PointerSpecifier;
import cetus.hir.Procedure;
import cetus.hir.ProcedureDeclarator;
import cetus.hir.Specifier;
import cetus.hir.VariableDeclaration;

/**
 * convert file between OpenCL and CUDA
 * @author jack
 *
 */
public class FileFormatter {

	public static final String FILE_OPENCL = "cl";
	public static final String FILE_CUDA = "cu";
	public static final String FILE_UNKNOWN = "unknown";
	
	public static String[] HEADER = {"bidx", "bidy", "tidx", "tidy", "gridDimX", "gridDimY"};
	static Hashtable<String,String> CUDA_DEF = new Hashtable();
	static {
		CUDA_DEF.put("bidx", "(blockIdx.x)");
		CUDA_DEF.put("bidy", "(blockIdx.y)");
		CUDA_DEF.put("tidx", "(threadIdx.x)");
		CUDA_DEF.put("tidy", "(threadIdx.y)");
		CUDA_DEF.put("gridDimX", "(gridDim.x)");
		CUDA_DEF.put("gridDimY", "(gridDim.y)");
	}
	
	static Hashtable<String,String> OPENCL_DEF = new Hashtable();
	static {
		OPENCL_DEF.put("bidx", "(get_group_id(0))");
		OPENCL_DEF.put("bidy", "(get_group_id(1))");
		OPENCL_DEF.put("tidx", "(get_local_id(0))");
		OPENCL_DEF.put("tidy", "(get_local_id(1))");
		OPENCL_DEF.put("gridDimX", "(get_num_groups(0))");
		OPENCL_DEF.put("gridDimY", "(get_num_groups(1))");
	}

	
	public static Hashtable<String,String> getDefTable(String type) {
		Hashtable<String,String> table = CUDA_DEF;
		if (type.equals(FILE_OPENCL)) table = OPENCL_DEF;
		return table;

	}
	public static String detect(Procedure proc) {
		List<Specifier> types = proc.getReturnType();
		for (Specifier type: types) {
			if (type.equals(Specifier.GLOBAL)) {
				return FILE_CUDA;
			}
			else 
			if (type.equals(Specifier.OPENCL_KERNEL)) {
				return FILE_OPENCL;
			}
		}
		return FILE_UNKNOWN;
	}
	
	
	public static Procedure convertCuda2OCl(Procedure procedure) {
//		System.out.println("in------------------");
//		System.out.println(procedure);
		List<Specifier> types = procedure.getReturnType();

		List<Specifier> clTypes = new ArrayList();
		for (Specifier type: types) {
			if (type.equals(Specifier.GLOBAL)) {
				clTypes.add(Specifier.OPENCL_KERNEL);
			}
			else {
				clTypes.add(type);				
			}
		}
		
		IDExpression clName = (IDExpression)procedure.getName().clone();
		
		List<Declaration> clParameters = new ArrayList();
		List<VariableDeclaration> sbs = (List<VariableDeclaration>)procedure.getParameters();
		for (int i=0; i<sbs.size(); i++) {
			VariableDeclaration id = (VariableDeclaration)sbs.get(i).clone();
			Declarator dec = (Declarator)id.getDeclarator(0);
			boolean isGlobal = false;
			for (Specifier spe: dec.getSpecifiers()) {
				if (spe instanceof PointerSpecifier) {
					isGlobal = true;
				}
			}
			if (isGlobal) {
				List list = id.getSpecifiers();
				list.add(0, Specifier.OPENCL_GLOBAL);
				VariableDeclaration ndec = new VariableDeclaration(list, dec);
				clParameters.add(ndec);
			}
			else {
				clParameters.add((VariableDeclaration)id);
			}
		}

		ProcedureDeclarator declarator = new ProcedureDeclarator(clName, clParameters);
		Procedure clProc = new Procedure(clTypes, declarator, (CompoundStatement)procedure.getBody().clone());
		
		BreadthFirstIterator bfi = new BreadthFirstIterator(clProc);
		List<VariableDeclaration> vds = (List<VariableDeclaration>)bfi.getList(VariableDeclaration.class);
		for (VariableDeclaration vd: vds) {
			if (vd.getSpecifiers().size()==0) continue;
			Specifier spe = vd.getSpecifiers().get(0);
			if (spe==Specifier.SHARED) {
				vd.getSpecifiers().set(0, Specifier.OPENCL_LOCAL);
			}
		}
		
		bfi = new BreadthFirstIterator(clProc);
		List<FunctionCall> fs = (List<FunctionCall>)bfi.getList(FunctionCall.class);
		for (FunctionCall fun: fs) {
			if (fun.toString().equals(getSyn().toString())) {
				fun.swapWith(getBarries());
			}
		}		
		
//		System.out.println("out------------------");
//		System.out.println(clProc);
		
		return clProc;

	}

	//barrier(CLK_LOCAL_MEM_FENCE);
	public static FunctionCall getBarries() {
		List list = new ArrayList();
		list.add(new Identifier("CLK_LOCAL_MEM_FENCE"));
		return new FunctionCall(new Identifier("barrier"), list);
	}
	
	public static FunctionCall getSyn() {
		return new FunctionCall(new Identifier("__syncthreads"));
	}	
	
	public static Procedure convertOCl2Cuda(Procedure procedure) {
//		System.out.println("in------------------");
//		System.out.println(procedure);
		List<Specifier> types = procedure.getReturnType();

		List<Specifier> cudaTypes = new ArrayList();
		for (Specifier type: types) {
//			System.out.println(type);
			if (type.equals(Specifier.OPENCL_KERNEL)) {
				cudaTypes.add(Specifier.GLOBAL);
			}
			else {
				cudaTypes.add(type);				
			}
		}
		
		IDExpression cudaName = (IDExpression)procedure.getName().clone();
		
		List<Declaration> cudaParameters = new ArrayList();
		List<VariableDeclaration> sbs = (List<VariableDeclaration>)procedure.getParameters();
		for (int i=0; i<sbs.size(); i++) {
			VariableDeclaration id = (VariableDeclaration)sbs.get(i).clone();
			Declarator dec = (Declarator)id.getDeclarator(0);
			List list = id.getSpecifiers();
			List nlist = new ArrayList();

			
			for (int k=0; k<list.size(); k++) {
				Specifier spe = (Specifier)list.get(k);	
				if (!spe.equals(Specifier.OPENCL_GLOBAL)) {
					nlist.add(spe);
				}
				
			}
			VariableDeclaration ndec = new VariableDeclaration(nlist, dec);
			cudaParameters.add(ndec);
		}

		ProcedureDeclarator declarator = new ProcedureDeclarator(cudaName, cudaParameters);
		Procedure cudaProc = new Procedure(cudaTypes, declarator, (CompoundStatement)procedure.getBody().clone());
		
		BreadthFirstIterator bfi = new BreadthFirstIterator(cudaProc);
		List<VariableDeclaration> vds = (List<VariableDeclaration>)bfi.getList(VariableDeclaration.class);
		for (VariableDeclaration vd: vds) {
			if (vd.getSpecifiers().size()==0) continue;
			Specifier spe = vd.getSpecifiers().get(0);
			if (spe==Specifier.SHARED) {
				vd.getSpecifiers().set(0, Specifier.OPENCL_LOCAL);
			}
		}
		
		bfi = new BreadthFirstIterator(cudaProc);
		List<FunctionCall> fs = (List<FunctionCall>)bfi.getList(FunctionCall.class);
		for (FunctionCall fun: fs) {
			if (fun.toString().equals(getBarries().toString())) {
				fun.swapWith(getSyn());
			}
		}			
//		System.out.println("cuda out------------------");
//		System.out.println(cudaProc);
		return cudaProc;
	}
}
