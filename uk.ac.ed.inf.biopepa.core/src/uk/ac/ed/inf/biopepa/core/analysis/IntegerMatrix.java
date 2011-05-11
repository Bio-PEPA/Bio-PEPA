package uk.ac.ed.inf.biopepa.core.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import Jama.Matrix; 
/**
 * Yet another matrix class, specifics are: fourier-motzkin to obtain invariants
 * which implies uncommon operations to remove rows or add sums of other rows.
 * 
 * @author peterkemper
 *
 */
public class IntegerMatrix implements Cloneable {
	/**
	 * matrix dimensions
	 */
	private int numRows = 0 ; 
	private int numCols = 0 ;
	/**
	 * internal storage format: a matrix is a vector of rows, each row is a array of int values
	 */
	private Vector<int[]> matrix ;
	/**
	 * Constructor
	 */
	public IntegerMatrix(int rows, int cols)
	{
		numRows = rows ;
		numCols = cols ;
		matrix = new Vector<int[]>(rows) ;
		for (int i = 0 ; i < numRows ; i++)
		{
			matrix.add(new int[cols]) ;
		}
	}
	/**
	 * private constructor for a more direct generation of a matrix
	 * @param rows
	 * @param cols
	 * @param entries
	 */
	/*
	private IntegerMatrix(int rows, int cols, Vector<int[]>entries)
	{
		numRows = rows ;
		numCols = cols ;
		matrix = entries ;
	}
	*/
	/**
	 * get number of rows
	 */
	public int getRowDimension()
	{
		return numRows ;
	}
	/**
	 * get number of columns
	 */
	public int getColumnDimension()
	{
		return numCols ;
	}
	/**
	 * get matrix entry
	 */
	public int get(int row, int col)
	{
		int[] d = matrix.get(row) ;
		return d[col] ;
	}
	/**
	 * set matrix entry
	 */
	public void set(int row, int col, int value)
	{
		int[] d = matrix.get(row) ;
		d[col] = value ;
	}
	/**
	 * add matrix entry
	 */
	public void add(int row, int col, int value)
	{
		int[] d = matrix.get(row) ;
		d[col] += value ;
	}
	/**
	 * sets all entries in the given row to zero
	 */
	public void setRowToZero(int row)
	{
		int[] d = matrix.get(row) ;
		for (int i = 0 ;i < d.length ; i++)
		{
			d[i] = 0 ;
		}
	}
	/**
	 * sets all entries in the given column to zero
	 */
	public void setColumnToZero(int col)
	{
		int[] row ;
		for (int i = 0 ; i < numRows ; i++)
		{
			row = matrix.get(i) ;
			row[col] = 0 ;
		}
	}
	/**
	 * linear algebraic matrix matrix multiplication resulting in new matrix
	 * @return new matrix c = this * m 
	 */
	public IntegerMatrix mult(IntegerMatrix m)
	{
		// defined iff this.numCols == m.numRows
		if (numCols != m.getRowDimension())
		{
			System.out.println("Matrix multiplication failed, dimensions don't match: try AxB with A: " 
					+ numRows + "x" + numCols + " and B:" + m.getRowDimension() + "x" + m.getColumnDimension()) ;
			return new IntegerMatrix(0,0) ;
		}
		// own dimension numRows x numCols
		// m dimension m.numRows x m.numCols
		// result has dimension numRows x m.numCols
		int cNumRows = numRows ;
		int cNumCols = m.getColumnDimension() ;
		IntegerMatrix C = new IntegerMatrix(numRows,cNumCols) ;
		// more common notation: C = A x B
		// where A == this.matrix, B == m, C == result
		int[] Ai = null ;
		// for each row
		for (int i = 0 ; i < cNumRows ; i++)
		{
			
			Ai = matrix.get(i) ;
			// multiply with each column
			for (int j = 0 ; j < cNumCols ; j++)
			{
				// new entry Cij = sum_k Aik * Bkj
				for (int k = 0 ; k < numCols ; k++)
				{
					//int Aik = Ai[k] ;
					//int Bkj = m.get(k, j) ;
					//int Cij += Aik * Bkj ;
					C.add(i, j, Ai[k] * m.get(k, j)) ;
				}
			}
		}
		return C ;
	}
	/**
	 * Fourier Motzkin: transform this matrix to zero matrix, 
	 * return effect of same transformations to identity matrix as a result.
	 * Note that if this matrix is (nxm) then the returned matrix is (unknown x n).
	 * @return new matrix with report on transformations applied to identity matrix
	 */
	public IntegerMatrix solveFourierMotzkin()
	{
		//throw new RuntimeException("FourierMotzkin: sorry not implemented yet") ;
		// Fourier Motzkin: 
		// given matrix == A, identity matrix I
		// consider A || I
		// transform A step by step into a 0 matrix
		// each steps considers a particular column j
		// splits rows into groups minus (m), plus(p), neutral(n) according to its entry in column j being <0, >0, ==0
		// keeps group n
		// removes groups m and p
		// for each pair of elements (m,p) add a new row to the result that is a linear combination with column entry j being 0
		//return null ;
		ArrayList<int[]> lminus = new ArrayList<int[]>() ;
		ArrayList<int[]> lplus = new ArrayList<int[]>() ;
		ArrayList<int[]> lneutral = new ArrayList<int[]>() ;
		ArrayList<int[]> lresult = new ArrayList<int[]>() ;
		// Method has exponential worst case for space and time, so we define a threshold to artificially
		// limit calculations
		final int threshold = 10000 ;
		// initial set up
		//System.out.println("Incidence matrix:");
		//System.out.println(printMatrix());
		for (int i = 0 ; i < numRows ; i++)
		{
			// create new vector: 
			int[] v = new int[numRows+numCols] ;
			int[] row = matrix.get(i) ;
			// copy corresponding row from matrix
			for (int j = 0 ; j < numCols ; j++)
			{
				v[j] = row[j] ;
			}
			// add the identity matrix part at the end
			v[numCols+i] = 1 ;
			// new vector is completely initialized, now put it in the appropriate set
			lneutral.add(v) ;
			
		}
		// iterate while first part is non-zero
		try 
		{
		int position = selectposition(lneutral,threshold) ;
		for (int i = 0 ; (i < numCols) && (-1 != position) ; i++ )
		{
			//System.out.println("FM Step:" + i);
			split(lneutral,lplus,lminus, position) ;
			addcombinations(lneutral,lplus,lminus,lresult,position) ;
			position = selectposition(lneutral, threshold) ;
			if (lneutral.size() > threshold)
				throw new Exception() ;
		}
		// reduce vectors to appropriate size (remove initial zero part
		lresult.addAll(lneutral) ;
		}
		catch(Exception ex)
		{
			// assume threshold is reached
			System.out.println("WARNING: invariant calculation artificially terminated, intermediate solution exceeds threshold of " + threshold);
			// invariants we obtained so far a perfectly fine results
			return reducedMatrix(lresult) ;
		}
		return reducedMatrix(lresult) ;
	}
	/**
	 * select the column the gives the least number of combinations
	 * @param lneutral
	 * @return
	 */
	private int selectposition(ArrayList<int[]> lneutral, int threshold) throws Exception
	{
		int min = Integer.MAX_VALUE ;
		int position = -1 ;
		int plus = 0 ;
		int minus = 0 ;
		int result = 0 ;
		int[] v ;
		for (int i = 0 ; i < numCols ; i++ )
		{
			plus = 0 ;
			minus = 0 ;
			for (int j = 0 ; j < lneutral.size() ; j++)
			{
				v = lneutral.get(j) ;
				if (v[i] < 0)
					minus++ ;
				if (v[i]>0)
					plus++ ;
			}
			if (0 == minus && 0 == plus)
				continue ; // column has been resolved before, skip this one
			
			result = minus*plus ;
			//update min if necessary
			if (result < min)
			{
				min = result ;
				position = i ;
			}
			// check special case for early termination
			if (1 >= result)
			{
				position = i ; // memo result
				i = numCols ;// terminate loop
			}
				
		}
		
		//System.out.println("suggest position " + position + " minus: " + minus + " plus: " + plus);
		if (result > threshold)
			throw new Exception() ;
		return position;
	}
	/**
	 * create a matrix from the last numCols ... numCols+numRows-1 entries in the vectors of the given list
	 * @param lneutral
	 * @return
	 */
	private IntegerMatrix reducedMatrix(ArrayList<int[]> lneutral) {
		IntegerMatrix result = new IntegerMatrix(lneutral.size(),numRows) ;
		// check special case of an empty set
		if (0 == lneutral.size())
		{
			// System.out.println("special case: set of computed invariants is empty") ;
			return result ;
		}
		// ok, result is not empty and contains at least one row
		Iterator<int[]> it = lneutral.iterator() ;
		int[] v = null ;
		int r = 0 ; // counter to access the current row in the result matrix
		while (it.hasNext())
		{
			v = it.next() ;
			for (int i = 0 ; i < numRows; i++)
			{
				result.set(r, i, v[numCols+i]) ; 
			}
			r++ ;
		}
		//System.out.println( " result of fourier motzkin:");
		//System.out.println(result.printMatrix());
		//return result ;
		//System.out.println(" calling base of row vectors");
		return baseOfRowVectors(result) ;
	}
	/**
	 * create a matrix with a maximal set of linear independent rows for the given matrix
	 * @param m input matrix
	 * @return matrix with a subset of rows copied from m
	 */
	private IntegerMatrix baseOfRowVectors(IntegerMatrix input)
	{
		int m = input.getRowDimension() ;
		int n = input.getColumnDimension() ;
		int[] support = new int[m] ; // number of nonzero entries per row, rows with few entries are preferable
		int[] base = new int[m] ; // flag: 0: not selected, 1: selected, -1: linear dependent
		int[] covered = new int[n] ; // flag: number of base(!) vectors that cover this column
		int selected = 0 ;
		// case 0: special case: 1 row only: all rows are linearly independent: copy matrix and return result
		if (1 == m)
		{
			// System.out.println("Integermatrix.baseOfRowVectors: special case, single row must be lin. independent!");
			return input.clone() ;
		}
		// compute the rank of the input matrix first to see how many vectors we need to select
		// compute the support of each row (number of nonzero entries in each row)
		Matrix mat = new Matrix(m,n) ;
		for (int i = 0 ; i < m ; i++)
		{
			for (int j=0 ; j < n ; j++)
			{
				if (input.get(i, j) != 0)
				{
					support[i] += 1 ;
					mat.set(i, j, input.get(i, j)) ; // implicit type conversion int -> double for entry
				}
			}
		}
		int rank = mat.rank() ;
		// System.out.println("matrix: " + m + " times " + n + ", rank computed: " + rank) ;
		// case 1: all rows are linearly independent: copy matrix and return result
		if (m == rank)
		{
			// System.out.println("Integermatrix.baseOfRowVectors: special case, all rows lin. independent!");
			return input.clone() ;
		}
		// select vectors that individually cover a particular column and have a small support
		// due to the coverage, those vectors MUST be linearly independent
		// for each column that is not covered pick a vector with small support
		int pos = 0 ;
		for (int j = 0 ; j < n ; j++)
		{
			if (0 < covered[j])
				continue ; // column is already covered
			pos = findRowWithMinimumSupport(m, support, base, j, input);
			// if there is a candidate row, select it, otherwise just continue
			if (0 <= pos)
			{
				base[pos] = 1 ; // selected
				selected++ ;
				for (int k = 0 ; k < n; k++)
				{
					if (0 != input.get(pos, k))
						covered[k] +=1 ; // update coverage
				}
			}
		}
		// we have an initial selection that is linearly independent, 
		// check if we are done or if there is room for more invariants to add
		// System.out.println("IntegerMatrix.baseOfRowVectors: initial selection yields " + selected + " out of " + rank + " vectors.");

		// let's look for the rest
		while (rank != selected)
		{
			// pick unchecked row with smallest support
			pos = findRowWithMinimumSupport(m, support, base);
			// if there is a candidate row, check it, 
			if (0 < pos)
			{
				// check if it is independent
				base[pos] = 1 ; // only temporary for matrix extraction
				selected++ ; 	// only temporary for matrix extraction
				Matrix matrix = extractSelectedRows2(input, m, n, base, selected) ; 
				int mrank = matrix.rank() ;
				//System.out.println("Row " + pos + " gives rank " + mrank + " for total rows: " + selected) ;
				if (selected == mrank)
				{
					// it is independent, so we can select it for the base
					//System.out.println("adding row " + pos);
					for (int k = 0 ; k < n; k++)
					{
						if (0 != input.get(pos, k))
							covered[k] +=1 ; // update coverage
					}
				}
				else
				{
					// not selected, we need to take back previous assignments
					//System.out.println("not adding row " + pos);
					base[pos] = -1 ;
					selected-- ;
				}
			}
			// else we ran out of candidates and must stop!
			else
			{
				if (rank != selected)
				{
					System.out.println("IntegerMatrix.baseOfRowVectors: no vectors left but rank " + rank + " does not match selection " + selected);
					rank = selected ; // terminate loop
				}
			}
		}
		// at this point: (rank == selected)
		// return selection
		System.out.println("base of Row vectors: base has dimension " + selected );
		return extractSelectedRows(input, m, n, base, selected);
	}
	/**
	 * private helper method to extract a subset of selected rows from an integer matrix
	 * @param input
	 * @param m
	 * @param n
	 * @param base
	 * @param selected
	 * @return
	 */
	private IntegerMatrix extractSelectedRows(IntegerMatrix input, int m, int n, int[] base, int selected) 
	{
		IntegerMatrix result = new IntegerMatrix(selected,n) ;
		int pos = 0 ;
		for (int i=0 ; i< m ; i++)
			{
				if (base[i] != 1)
					continue ;
				// copy entries into result matrix
				for (int j=0 ; j < n ;j++)
				{
					result.set(pos, j, input.get(i,j)) ;
				}
				pos++ ;
			}
		System.out.println("finally returned: " + pos + " for selected " + selected);
		return result;
	}
	/**
	 * private helper method to extract a subset of selected rows from an integer matrix
	 * @param input
	 * @param m
	 * @param n
	 * @param base
	 * @param selected
	 * @return
	 */
	private Matrix extractSelectedRows2(IntegerMatrix input, int m, int n, int[] base, int selected) 
	{
		Matrix result = new Matrix(selected,n) ;
		int pos = 0 ;
		for (int i=0 ; i< m ; i++)
			{
				if (base[i] != 1)
					continue ;
				// copy entries into result matrix
				for (int j=0 ; j < n ;j++)
				{
					result.set(pos, j, input.get(i,j)) ; // implicit type conversion: int -> double
				}
				pos++ ;
			}
		return result;
	}
	/**
	 * private helper method
	 * @param m
	 * @param support
	 * @param base
	 * @return -1 if nothing is found, value >=0 for valid row index
	 */
	private int findRowWithMinimumSupport(int m, int[] support, int[] base) {
		int min = Integer.MAX_VALUE ;
		int pos = -1 ;
		for (int i =0 ; i < m ; i++)
		{
			// pick row with smallest support that has not been selected yet (or recognized dependent)
			if (0 < support[i] && support[i] < min && 0 == base[i])
			{
				min = support[i] ;
				pos = i ;
			}
		}
		//System.out.println("Picking row " + pos + " due to minimal support " + min);
		return pos;
	}
	private int findRowWithMinimumSupport(int m, int[] support, int[] base, int column, IntegerMatrix input) {
		int min = Integer.MAX_VALUE ;
		int pos = -1 ;
		for (int i =0 ; i < m ; i++)
		{
			// pick row with smallest support that has not been selected yet (or recognized dependent)
			if (0 < support[i] && support[i] < min && 0 == base[i] && 0 != input.get(i, column))
			{
				min = support[i] ;
				pos = i ;
			}
		}
		//System.out.println("Picking row " + pos + " due to minimal support " + min + " for column " + column);
		return pos;
	}
	/**
	 * 
	 * @param lneutral
	 * @param lplus
	 * @param lminus
	 * @param lresult
	 * @param position
	 */
	private void addcombinations(ArrayList<int[]> lneutral,
			ArrayList<int[]> lplus, ArrayList<int[]> lminus, ArrayList<int[]> lresult, int position) {
		// case 1: lminus is empty, then clean up lplus since there are no combinations to create
		if (lminus.isEmpty())
		{
			// clean up lplus in any case
			lplus.clear() ;
			return ;
		}
		// case 2: lplus is empty, then clean up lminus since there are no combinations to create
		if (lplus.isEmpty())
		{
			// clean up lminus in any case
			lminus.clear() ;
			return ;
		}
		// case 3: both lists are not empty, we need to create |lminus| * |lplus| combinations
		Iterator<int[]> itminus = lminus.iterator() ;
		Iterator<int[]> itplus = null ;
		int[] vm ;
		int[] vp ;
		int gcd ;
		int scalep ;
		int scalem ;
		int[] vn ;
		boolean finished ;
		while (itminus.hasNext())
		{
			vm = itminus.next() ;
			itplus = lplus.iterator() ;
			while (itplus.hasNext())
			{
				vp = itplus.next() ;
				//System.out.println("vp: " + debugprinter(vp));
				//System.out.println("vm: " + debugprinter(vm));
				// get least common multiple
				// gcd = P3InvariantImplementation.bigGCD(Math.abs(vm[position]), vp[position], false) ;
				// gcd    = myGCD(Math.abs(vm[position]), vp[position], false);
				
				gcd = euklidGCD(vm[position], vp[position]);
				scalep = Math.abs(vm[position]) / gcd ;
				scalem = vp[position] / gcd ;
				//System.out.println("gcd:" + gcd + " scalep" + scalep + "scalem" + scalem);
				vn = new int[vm.length] ;
				// check correctness
				if (0 != vm[position]*scalem + vp[position]*scalep )
					throw new RuntimeException("Calculation of combinations is wrong") ;
				// since positions are selected in any order, we need to add all entries of the vectors
				finished = true ; 
				for (int i = 0  ; i < vm.length ; i++)
				{
					vn[i] = vm[i]*scalem + vp[i]*scalep ;
					if (i<numCols && 0 !=vn[i])
						finished = false ;
				}
				normalizeWithGCD(vn,0);
				//System.out.println("vn: " + debugprinter(vn));
				// add only if really new
				//add(lneutral,vn,position) ;
				if (finished)
					add(lresult,vn,0) ;
				else
					add(lneutral,vn,0) ;
				//lneutral.add(vn) ;
			}
		}
		lminus.clear() ;
		lplus.clear() ;
	}
	private void add(ArrayList<int[]> lneutral, int[] vn, int startposition) {
		// check if vector is really new
		
		if (containsVector(lneutral, vn, startposition))
			return ;
		// vector vn survived duplicate filter and needs to be added
		lneutral.add(vn) ;
		//System.out.println("vn added");
	}
	/**
	 * check if the given list already contains the given vector
	 * check entries only starting at the given startposition
	 * @param lneutral
	 * @param vn
	 * @param startposition
	 * @return
	 */
	private boolean containsVector(ArrayList<int[]> lneutral, int[] vn,
			int startposition) {
		int[] v;
		boolean ok;
		// check all existing vectors in lneutral
		for (int i = 0 ; i < lneutral.size() ; i++)
		{
			v = lneutral.get(i) ;
			ok = false ;
			// we only need to consider parts that are nonzero 
			// which are known to start at startposition the earliest
			for (int j=startposition ; j < v.length ; j++)
			{
				if (v[j] != vn[j])
				{
					// so vectors are really different, stop the loop
					ok = true ;
					j = v.length ;
				}
			}
			if (!ok)
			{
				// vector is a duplicate, skip
				//System.out.println("skip duplicate vector");
				return true ;
			}
		}
		return false ;
	}
	
	/*
	 * My simple version of the greatest common divisor routine
	 * temporarily used to avoid the unresolved reference to
	 * P3InvariantImplementation.bigGCD
	 * 
	 */
	private int myGCD(int a, int b, boolean notsure) {
		   if (b==0) 
		     return a;
		   else
		     return myGCD(b, a % b, notsure);
    } 
	
	
	private static int euklidGCD(int a, int b) {
		// function gcd(a, b)
		// while b ï¿Ω 0
		// t := b
		// b := a mod b
		// a := t
		// return a
		// System.out.print("GCD (" + a + ", " + b + ") = ") ;
		a = Math.abs(a);
		b = Math.abs(b);
		int t;
		while (0 != b) {
			t = b;
			b = a % b;
			a = t;
		}
		// System.out.println(a);
		return a;
	}
	
	/**
	 * compute the gcd and normalize the entries of the given vector
	 * consider only entries starting at the given startposition in the vector
	 * @param vn
	 * @param startposition
	 */
	private void normalizeWithGCD(int[] vn, int startposition) {
		// compute the gcd
		int gcd = 0 ;
		for (int i = startposition ; i < vn.length ; i++)
		{
			if (0 != vn[i])
			{
				if (0 == gcd)
				{
					// initialize
					gcd = Math.abs(vn[i]) ;
				}
				else
				{
					// gcd = P3InvariantImplementation.bigGCD(gcd, Math.abs(vn[i]), false) ;
					// gcd = myGCD(gcd, Math.abs(vn[i]), false);
					gcd = euklidGCD(gcd, vn[i]);
				}
			}
		}
		// normalize 
		if (0 != gcd && 1 != gcd)
		{
			for (int i = startposition ; i < vn.length ; i++)
			{
				if (0 != vn[i])
				{
					vn[i] /= gcd ;
				}
			}
		}
	}
	/*
	private String debugprinter(int[] v)
	{
		String result = "" ;
		for (int i=0 ; i <v.length ; i++)
			result += v[i]+" " ;
		return result ;
	}
	*/
	/**
	 * move elements from the lneutral list to lplus or lminus depending on the nonzero entries in the given column
	 * @param lneutral
	 * @param lplus
	 * @param lminus
	 * @param column
	 */
	private void split(ArrayList<int[]> lneutral, ArrayList<int[]> lplus,
			ArrayList<int[]> lminus, int column) 
	{
		int i = 0 ;
		int[] v ;
		while (i < lneutral.size())
		{
			v = lneutral.get(i) ;
			if (v[column] < 0)
			{
				lminus.add(v) ;
				lneutral.remove(i) ;
			}
			else
			{
				if (v[column] > 0)
				{
					lplus.add(v) ;
					lneutral.remove(i) ;
				}
				else
				{
					i++ ;
				}
			}
		}
		//System.out.println("FM Split gives neutral: " + lneutral.size() + " plus" + lplus.size() + " minus:" + lminus.size());
	}
	/**
	 * transpose matrix into new one
	 */
	public IntegerMatrix transpose()
	{
		IntegerMatrix result = new IntegerMatrix(numCols,numRows) ;
		int[] row = null ;
		for (int i = 0 ; i < numRows ; i++)
		{
			row = matrix.get(i) ;
			for (int j = 0 ; j < numCols ; j++)
			{
				result.set(j, i, row[j]) ;
			}
		}
		return result ;
	}
	/**
	 * clones the whole matrix onto new memory
	 */
	public IntegerMatrix clone()
	{
		IntegerMatrix result = new IntegerMatrix(numRows,numCols) ;
		int[] row = null ;
		for (int i = 0 ; i < numRows ; i++)
		{
			row = matrix.get(i) ;
			for (int j = 0 ; j < numCols ; j++)
			{
				result.set(i, j, row[j]) ;
			}
		}
		return result ;
	}
	/**
	 * checks equality of elements up to an epsilon
	 */
	public boolean equals(IntegerMatrix m)
	{
		return (0 == compare(m)) ;
	}
	/**
	 * compares with matrix, equality up to an epsilon
	 */
	public int compare(IntegerMatrix m)
	{
		// check if dimensions match
		if (numCols != m.getColumnDimension() || numRows != m.getRowDimension())
			throw new RuntimeException("Cannot compare matrices of different dimensions") ;
		int[] row = null ;
		int result = 0 ; // so far everything equal
		for (int i = 0 ; i < numRows ; i++)
		{
			row = matrix.get(i) ;
			for (int j = 0 ; j < numCols ; j++)
			{
				if (row[j]!= m.get(i,j))
				{
					switch (result) 
					{
					case -1 : 
						if (row[j] > m.get(i, j))
							throw new RuntimeException("Matrices are not comparable") ;
						break ;
					case 0 : 
						result = (row[j] < m.get(i, j)) ? -1 : 1 ;
						break ; 
					case 1 : 
						if (row[j] < m.get(i, j))
							throw new RuntimeException("Matrices are not comparable") ;
						break ;
					default :
						throw new RuntimeException("Reaching unreachable default case") ;
					}
				}
			}
		}
		return result ;
	}
	/**
	 * tell if this is a matrix with zero entries only
	 */
	public boolean isZeroMatrix()
	{
		// check if the result is the zero vector
		int[] row = null ;
		for (int i = 0 ; i < numRows ; i++)
		{
			row = matrix.get(i) ;
			for (int j = 0 ; j < numCols ; j++)
			{
				if (0 != row[j])
					return false ;
			}
		}
		return true ;
	}
	/**
	 * tell if this is a matrix with zero entries only but for those on the main diagonal,
	 * works for rectangular matrices as well
	 */
	public boolean isIdentityMatrix()
	{
		// check if the result is the zero vector
		int[] row = null ;
		for (int i = 0 ; i < numRows ; i++)
		{
			row = matrix.get(i) ;
			for (int j = 0 ; j < numCols ; j++)
			{
				if (i == j)
				{
					if (1 != row[j])
						return false ;
				}
				else
				{
					if (0 != row[j])
						return false ;
				}
			}
		}
		return true ;
	}
	public final static String newline = System.getProperty("line.separator");
	/**
	 * print matrix to see its content
	 */
	public String printMatrix()
	{
		String result = "Content of an " + numRows + " x " + numCols + " matrix:" + newline ;
		for (int i = 0 ; i < numRows ; i++)
		{
			result += "row " + i + ": " ;
			for (int j = 0 ; j< numCols ; j++)
			{
				result += " " + this.get(i, j) ;
				
			}
			result += newline ;
		}
		return result ;
	}
}
