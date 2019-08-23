package base_UI_Objects.windowUI;

import java.io.*;
import java.util.*;

import base_UI_Objects.my_procApplet;
import base_UI_Objects.drawnObjs.myDrawnSmplTraj;
import base_Utils_Objects.vectorObjs.myPoint;
import base_Utils_Objects.vectorObjs.myVector;

//displays sidebar menu of interaction and functionality

public abstract class BaseBarMenu extends myDispWindow{

	//booleans in main program - need to have labels in idx order, even if not displayed
	public final String[] truePFlagNames = {//needs to be in order of flags
			"Debug Mode",
			"Final init Done",
			"Save Anim", 	
			"Key Pressed",
			"Shift-Key Pressed",
			"Alt-Key Pressed",
			"Cntl-Key Pressed",
			"Click interact", 	
			"Drawing Curve",
			"Changing View",	
			"Stop Simulation",
			"Single Step",
			"Displaying Side Menu",
			"Displaying UI Menu",
			"Reverse Drawn Trajectory"
			};
	
	public final String[] falsePFlagNames = {//needs to be in order of flags
			"Debug Mode",	
			"Final init Done",
			"Save Anim", 	
			"Key Pressed",
			"Shift-Key Pressed",
			"Alt-Key Pressed",
			"Cntl-Key Pressed",
			"Click interact", 	
			"Drawing Curve",
			"Changing View",	 	
			"Run Simulation",
			"Single Step",
			"Displaying Side Menu",
			"Displaying UI Menu",
			"Reverse Drawn Trajectory"
			};
	
	protected static final float xLblOffsetMult = .02f;
	
	public int[][] pFlagColors;
	private final List<Integer> mainFlagsToShow;
	private final int numMainFlagsToShow;
	
	public final int clkFlgsStY = 10;
	
	public final String[] StateBoolNames = {"Shift","Alt","Cntl","Click", "Draw","View"};
	//multiplier for displacement to display text label for stateboolnames
	public final float[] StrWdMult = new float[]{-3.0f,-3.0f,-3.0f,-3.2f,-3.5f,-2.5f};
	public int[][] stBoolFlagColors;


	//private child-class flags - window specific
	public static final int 
			mseClickedInBtnsIDX 		= 0,					//the mouse was clicked in the button region of the menu and a click event was processed
			usesWinBtnDispIDX			= 1,					//this menu displays the window title bar
			usesMseOvrBtnDispIDX		= 2,					//this menu uses mouse-over display text
			usesDbgBtnDispIDX			= 3;					//this menu displays debug side bar buttons
	//private flag based buttons - ui menu won't have these
	public static final int numPrivFlags = 4;
	
	//GUI Buttons
	public float minBtnClkY;			
	//where buttons should start on side menu
	//index in arrays of button states/names for the show window buttons, if present
	public static final int btnShowWinIdx = 0;
	public static int btnMseFuncIdx, btnDBGSelCmpIdx; 
	//array of the idxs in the guiBtnArrays where the user-definable/modifiable functions reside
	protected int[] funcBtnIDXAra;
	//offset in arrays where actual function buttons start
	public int funcBtnIDXOffset=0;
	
	private boolean _initBtnShowWin = false, _initBtnMseFunc= false, _initBtnDBGSelCmp = false;

	private String[] guiBtnRowNames;
	//names for each row of buttons - idx 1 is name of row
	private String[][] guiBtnNames;
	//default names, to return to if not specified by user
	private String[][] defaultUIBtnNames;
	//whether buttons are momentary or not (on only while being clicked)
	private Boolean[][] guiBtnInst;
	//whether buttons are waiting for processing to complete (for non-momentary buttons)
	private Boolean[][] guiBtnWaitForProc;
	
	//whether buttons are disabled(-1), enabled but not clicked/on (0), or enabled and on/clicked(1)
	private int[][] guiBtnSt;
	
	public final int[] guiBtnStFillClr = new int[]{		//button colors based on state
			my_procApplet.gui_White,								//disabled color for buttons
			my_procApplet.gui_LightGray,								//not clicked button color
			my_procApplet.gui_LightBlue,									//clicked button color
		};
	public final int[] guiBtnStTxtClr = new int[]{			//text color for buttons
			my_procApplet.gui_LightGray,									//disabled color for buttons
			my_procApplet.gui_Black,									//not clicked button color
			my_procApplet.gui_Black,									//clicked button color
		};	
	//row and column of currently clicked-on button (for display highlight as pressing)
	public int[] curBtnClick = new int[]{-1,-1};
//	public BaseBarMenu(my_procApplet _p, String _n, int _flagIdx, int[] fc, int[] sc, float[] rd, float[] rdClosed, String _winTxt, boolean _canDrawTraj) {
//		super(_p, _n, _flagIdx, fc, sc,  rd, rdClosed, _winTxt, _canDrawTraj);

	public BaseBarMenu(my_procApplet _p, String _n, int _flagIdx, int[] fc, int[] sc, float[] rd, float[] rdClosed, String _winTxt) {
		super(_p, _n, _flagIdx, fc, sc,  rd, rdClosed, _winTxt);
		//these have to be set before setupGUIObjsAras is called from initThisWin
		numMainFlagsToShow = pa.getNumFlagsToShow();
		mainFlagsToShow = pa.getMainFlagsToShow();
		//super.initThisWin(_canDrawTraj, false, true);
		super.initThisWin(true);
	}
	
	//set up initial colors for papplet's flags for display
	public void initPFlagColors(){
		pFlagColors = new int[pa.numBaseFlags][3];
		for (int i = 0; i < pa.numBaseFlags; ++i) { pFlagColors[i] = new int[]{(int) pa.random(150),(int) pa.random(100),(int) pa.random(150)}; }		
		stBoolFlagColors = new int[pa.numStFlagsToShow][3];
		stBoolFlagColors[0] = new int[]{255,0,0};
		stBoolFlagColors[1] = new int[]{0,255,0};
		stBoolFlagColors[2] = new int[]{0,0,255};		
		for (int i = 3; i < pa.numStFlagsToShow; ++i) { stBoolFlagColors[i] = new int[]{100+((int) pa.random(150)),150+((int) pa.random(100)),150+((int) pa.random(150))};		}
	}
		
	/**
	 * call this from each new window to set function btn names, if specified, when window gets focus
	 * @param rowIdx
	 * @param btnNames
	 */
	public void setAllFuncBtnNames(int _funRowIDX, String[] btnNames) {
		int rowIdx = funcBtnIDXAra[_funRowIDX];		
		String[] replAra = ((null==btnNames) || (btnNames.length != guiBtnNames[rowIdx].length)) ? defaultUIBtnNames[rowIdx] : btnNames;
		for(int i=0;i<guiBtnNames[rowIdx].length;++i) {guiBtnNames[rowIdx][i]=replAra[i];}
	}//setFunctionButtonNames
	
	/**
	 * set row names for each row of ui action buttons getMouseOverSelBtnNames()
	 * @param _funcRowNames array of names for each row of functional buttons 
	 * @param _numBtnsPerFuncRow array of # of buttons per row of functional buttons
	 * @param _numDbgBtns # of debug buttons
	 * @param _inclWinNames include the names of all the instanced windows
	 * @param _inclMseOvValues include a row for possible mouse over values
	 */
	protected final void setBtnData(String[] _funcRowNames, int[] _numBtnsPerFuncRow, int _numDbgBtns, boolean _inclWinNames, boolean _inclMseOvValues) {
		ArrayList<String> tmpGuiBtnRowNames = new ArrayList<String>();
		ArrayList<String[]> tmpBtnNames = new ArrayList<String[]>();
		ArrayList<String[]> tmpDfltBtnNames = new ArrayList<String[]>();
		
		ArrayList<Boolean[]> tmpBntIsInst = new ArrayList<Boolean[]>();
		ArrayList<Boolean[]> tmpBntWaitForProc = new ArrayList<Boolean[]>();
		int numFuncBtnArrayNames = _funcRowNames.length;
		if(_numDbgBtns > 0) {		++numFuncBtnArrayNames;	}
		
		funcBtnIDXAra = new int[numFuncBtnArrayNames];
		
		btnDBGSelCmpIdx = -1;
		btnMseFuncIdx = -1;
		funcBtnIDXOffset = 0;
		String[] titleArray = new String[pa.winTitles.length-1];		
		if((_inclWinNames)&&(titleArray.length != 0)) {
			for(int i=0;i<titleArray.length;++i) {titleArray[i] = pa.winTitles[i+1];}
			tmpBtnNames.add(titleArray);
			tmpDfltBtnNames.add(titleArray);

			tmpBntIsInst.add(buildDfltBtnFlagAra(titleArray.length));
			tmpBntWaitForProc.add(buildDfltBtnFlagAra(titleArray.length));
			
			tmpGuiBtnRowNames.add("Display Window");

			_initBtnShowWin = true;
			++funcBtnIDXOffset;
		} else {			_initBtnShowWin= false;		}
		
		String[] mseOvrBtnNames = pa.getMouseOverSelBtnNames();	
		if((_inclMseOvValues) && (mseOvrBtnNames!=null) && (mseOvrBtnNames.length > 0)) {
			tmpBtnNames.add(mseOvrBtnNames);
			tmpDfltBtnNames.add(mseOvrBtnNames);
		
			tmpBntIsInst.add(buildDfltBtnFlagAra(mseOvrBtnNames.length));
			tmpBntWaitForProc.add(buildDfltBtnFlagAra(mseOvrBtnNames.length));

			tmpGuiBtnRowNames.add("Mouse Over Info To Display");
			btnMseFuncIdx = funcBtnIDXOffset;
			++funcBtnIDXOffset;
			_initBtnMseFunc= true;
		} else {			_initBtnMseFunc= false;}
		
 		for(int i=0;i<_numBtnsPerFuncRow.length;++i) {
			String s = _funcRowNames[i];
			tmpBtnNames.add(buildBtnNameAra(_numBtnsPerFuncRow[i],"Func"));
			tmpDfltBtnNames.add(buildBtnNameAra(_numBtnsPerFuncRow[i],"Func"));
			
			tmpBntIsInst.add(buildDfltBtnFlagAra(_numBtnsPerFuncRow[i]));
			tmpBntWaitForProc.add(buildDfltBtnFlagAra(_numBtnsPerFuncRow[i]));
			funcBtnIDXAra[i]=i+funcBtnIDXOffset;
			tmpGuiBtnRowNames.add(s);
		}
		if(_numDbgBtns > 0) {
			tmpBtnNames.add(buildBtnNameAra(_numDbgBtns,"Debug"));
			tmpDfltBtnNames.add(buildBtnNameAra(_numDbgBtns,"Debug"));
			
			tmpBntIsInst.add(buildDfltBtnFlagAra(_numDbgBtns));
			tmpBntWaitForProc.add(buildDfltBtnFlagAra(_numDbgBtns));
			tmpGuiBtnRowNames.add("DEBUG");
			btnDBGSelCmpIdx = tmpGuiBtnRowNames.size()-1;
			funcBtnIDXAra[numFuncBtnArrayNames-1]=numFuncBtnArrayNames-1+funcBtnIDXOffset;
			_initBtnDBGSelCmp = true;
		} else {	_initBtnDBGSelCmp = false;	}
		
		guiBtnRowNames = tmpGuiBtnRowNames.toArray(new String[0]);		
		guiBtnNames = tmpBtnNames.toArray(new String[0][]);
		defaultUIBtnNames = tmpDfltBtnNames.toArray(new String[0][]);
		guiBtnInst = tmpBntIsInst.toArray(new Boolean[0][]);
		guiBtnWaitForProc = tmpBntWaitForProc.toArray(new Boolean[0][]);
		
		guiBtnSt = new int[guiBtnRowNames.length][];
		for(int i=0;i<guiBtnSt.length;++i) {guiBtnSt[i] = new int[guiBtnNames[i].length];}
		
	}
	
	public Boolean[][] getGuiBtnWaitForProc() {return guiBtnWaitForProc;}
	public void setGuiBtnWaitForProc(Boolean[][] _guiBtnWaitForProc) {		this.guiBtnWaitForProc = _guiBtnWaitForProc;}
	public int[][] getGuiBtnSt() {		return guiBtnSt;	}
	public void setGuiBtnSt(int[][] guiBtnSt) {	this.guiBtnSt = guiBtnSt;	}

	private String[] buildBtnNameAra(int numBtns, String prfx) {
		String[] res = new String[numBtns];
		for(int i=0;i<numBtns;++i) {res[i]=""+prfx+" "+(i+1);}
		return res;
	}
	private Boolean[] buildDfltBtnFlagAra(int numBtns) {
		Boolean[] tmpAra1 = new Boolean[numBtns];
		for(int i=0;i<tmpAra1.length;++i) {tmpAra1[i]=false;}
		return tmpAra1;
	}
	
	
	
	@Override
	//initialize all private-flag based UI buttons here - called by base class
	public final void initAllPrivBtns(ArrayList<Object[]> tmpBtnNamesArray){
	}//
	
	@Override
	protected final void initMe() {//init/reinit this window
		setFlags(closeable, false);
//		setFlags(uiObjsAreVert, true);
		initPrivFlags(numPrivFlags);	
		setPrivFlags(usesWinBtnDispIDX,_initBtnShowWin);	
		setPrivFlags(usesMseOvrBtnDispIDX,_initBtnMseFunc);	
		setPrivFlags(usesDbgBtnDispIDX,	_initBtnDBGSelCmp);
		
	}	
	
	/**
	 * initialize application-specific windows and titles in structs :
	 *  guiBtnRowNames, guiBtnNames, defaultUIBtnNames, guiBtnInst, guiBtnWaitForProc;
	 */
	protected abstract void initSideBarMenuBtns_Priv();
	//set flag values and execute special functionality for this sequencer
	@Override
	public final void setPrivFlags(int idx, boolean val){
		int flIDX = idx/32, mask = 1<<(idx%32);
		privFlags[flIDX] = (val ?  privFlags[flIDX] | mask : privFlags[flIDX] & ~mask);
		switch (idx) {//special actions for each flag
			case mseClickedInBtnsIDX 	: {break;}			
			case usesWinBtnDispIDX	 	: {break;}
			case usesMseOvrBtnDispIDX 	: {break;}
			case usesDbgBtnDispIDX	 	: {break;}
		}
	}

	//initialize structure to hold modifiable menu regions
	@Override
	protected final void setupGUIObjsAras(TreeMap<Integer, Object[]> tmpUIObjArray, TreeMap<Integer, String[]> tmpListObjVals){						//called from super.initThisWin
		//set up side bar menu buttons with format specific to instancing application
		initSideBarMenuBtns_Priv();
		
		guiMinMaxModVals = new double [][]{	{}};//min max mod values		
		guiStVals = new double[]{};
		guiObjNames = new String[]{};			
		//idx 0 is treat as int, idx 1 is obj has list vals, idx 2 is object gets sent to windows
		guiBoolVals = new boolean [][]{{}};
		
		
		minBtnClkY = (numMainFlagsToShow+3) * yOff + clkFlgsStY;										//start of buttons from under boolean flags
		//all ui ojbects for all windows will follow this format and share the x[0] value
		initUIClickCoords(rectDim[0] + xLblOffsetMult * rectDim[2],minBtnClkY + (guiBtnRowNames.length * 2.0f) * yOff,rectDim[0] + .99f * rectDim[2],0);//last val over-written by actual value in buildGuiObjs
		guiObjs = new myGUIObj[0];			//list of modifiable gui objects
//		TreeMap<Integer, String[]> listObjs = new TreeMap<Integer, String[]>();
//		if(0!=guiObjs.length){			buildGUIObjs(guiObjNames,guiStVals,guiMinMaxModVals,guiBoolVals, new double[]{xOff,yOff}, listObjs);		} 
//		else {			uiClkCoords[3] = uiClkCoords[1];	}	//set y start values
		uiClkCoords[3] = uiClkCoords[1]-20;
	}//setupGUIObjsAras
	
	//check if buttons clicked
	private boolean checkButtons(int mseX, int mseY){
		double stY = minBtnClkY + rowStYOff, endY = stY+yOff, stX = 0, endX, widthX; //btnLblYOff			
		for(int row=0; row<guiBtnRowNames.length;++row){
			widthX = rectDim[2]/(1.0f * guiBtnNames[row].length);
			stX =0;	endX = widthX;
			for(int col =0; col<guiBtnNames[row].length;++col){	
				if((pa.ptInRange(mseX, mseY,stX, stY, endX, endY)) && (guiBtnSt[row][col] != -1)){
					handleButtonClick(row,col);
					return true;
				}					
				stX += widthX;	endX += widthX; 
			}
			stY = endY + yOff+ rowStYOff;endY = stY + yOff;				
		}
		return false;
	}//handleButtonClick	
	//public void clearAllBtnStates(){for(int row=0; row<guiBtnRowNames.length;++row){for(int col =0; col<guiBtnNames[row].length;++col){if((guiBtnInst[row][col]) && (guiBtnSt[row][col] ==1)){	guiBtnSt[row][col] = 0;}}}}
	
	//turn off buttons that may be on and should be turned off - called at release of mouse - check for mouse loc before calling (in button region)?
	public final void clearAllBtnStates(){
		if(this.getPrivFlags(mseClickedInBtnsIDX)) {
			//guiBtnWaitForProc should only be set for non-momentary buttons when they are pushed and cleared when whatever they are do is complete
			for(int row=0; row<guiBtnRowNames.length;++row){for(int col =0; col<guiBtnNames[row].length;++col){				
				if((guiBtnSt[row][col]==1) && (guiBtnInst[row][col]  || !guiBtnWaitForProc[row][col])){	guiBtnSt[row][col] = 0;}//btn is on, and either is momentary or it is not waiting for processing
			}}
			this.setPrivFlags(mseClickedInBtnsIDX, false);
		}
	}//clearAllBtnStates
	
	/**
	 * clear the passed row of buttons except for the indicated btn - to enable single button per row radio-style buttons
	 * @param row row of buttons to clear
	 * @param btnToKeepOn btn whose state should stay on
	 */
	protected final void clearRowExceptPassedBtn(int row, int btnToKeepOn) {
		for(int col =0; col<guiBtnNames[row].length;++col){	guiBtnSt[row][col] = 0;}
		guiBtnSt[row][btnToKeepOn]=1;
	}
	
	//set non-momentary buttons to be waiting for processing complete comand
	public final void setWaitForProc(int row, int col) {
		if(!guiBtnInst[row][col]) {	guiBtnWaitForProc[row][col] = true;}		
	}
	//handle click on button region of menubar
	public final void handleButtonClick(int row, int col){
		int val = guiBtnSt[row][col];//initial state, before being changed
		guiBtnSt[row][col] = (guiBtnSt[row][col] + 1)%2;//change state
		//int newVal = guiBtnSt[row][col];//curr state, after being changed
		//if not momentary buttons, set wait for proc to true
		setWaitForProc(row,col);
		if((row == btnShowWinIdx) && this.getPrivFlags(usesWinBtnDispIDX)) {pa.handleShowWin(col, val);}
		else if((row == btnMseFuncIdx) && this.getPrivFlags(usesMseOvrBtnDispIDX)) {
			if(val==0) {clearRowExceptPassedBtn(row,col);}
			pa.handleMenuBtnMseOvDispSel(col, val==0);
			
		}
		else if((row == btnDBGSelCmpIdx) && this.getPrivFlags(usesDbgBtnDispIDX)) {pa.handleMenuBtnDebugSel(col, val);}
		else {pa.handleMenuBtnSelCmp(row, funcBtnIDXOffset, col, val);}
//		switch(row){
//			case btnShowWinIdx 			: {pa.handleShowWin(col, val);break;}
//			case btnAuxFunc1Idx 		: //{pa.handleMenuBtnSelCmp(btnAuxFunc1Idx,col, val);break;}
//			case btnAuxFunc2Idx 		: //{pa.handleMenuBtnSelCmp(btnAuxFunc2Idx,col, val);break;}
//			case btnAuxFunc3Idx 		: //{pa.handleMenuBtnSelCmp(btnAuxFunc2Idx,col, val);break;}
//			case btnAuxFunc4Idx			:
//			case btnDBGSelCmpIdx  		: {pa.handleMenuBtnSelCmp(row, col, val);break;}//{pa.handleMenuBtnSelCmp(btnDBGSelCmpIdx,col, val);break;}
////			case btnFileCmdIdx 			: {pa.handleFileCmd(btnFileCmdIdx, col, val);break;}
//		}
		
	}	
	
	@Override
	protected final void launchMenuBtnHndlr(int funcRow, int btn) {	}

	@Override
	public final void handleSideMenuMseOvrDispSel(int btn, boolean val) {	}

	@Override
	public final void handleSideMenuDebugSel(int btn, int val) {	}

	//uses passed time
	@Override //only send new values if actually new values
	protected void setUIWinVals(int UIidx){
		switch(UIidx){
//		//set lcl/global vals
//		case gIDX_UIElem2List 		: {
////			int sel = (int)guiObjs[UIidx].getVal() % keySigs.length;
////			if (sel != myDispWindow.glblKeySig.keyIdx){for(int i=1; i<pa.dispWinFrames.length; ++i){pa.dispWinFrames[i].setGlobalKeySigVal(sel);} pa.setFlags(pa.forceInKey,false); }			
//			break;}
//		case gIDX_UIElem3 	: 
//		case gIDX_UIElem3List 	: {
////			int tsDenom = timeSigDenom[(int)guiObjs[gIDX_UIElem3List].getVal() %timeSigDenom.length],
////					tsNum = (int)guiObjs[gIDX_TimeSigNum].getVal();
////			durType dType = pa.getDurTypeForNote(tsDenom);			
////			if((dType != glblBeatNote) || (glblTimeSig.beatPerMeas != tsNum) || (glblTimeSig.beatNote != tsDenom)){			
////				for(int i=1; i<pa.dispWinFrames.length; ++i){pa.dispWinFrames[i].setGlobalTimeSigVal(tsNum,tsDenom, dType);} 
////			}
//			break;}
//		case gIDX_UIElem1			: {
//			float tmpTempo = (float)guiObjs[UIidx].getVal();
////			if(PApplet.abs(tmpTempo - glblTempo) > pa.feps){for(int i=1; i<pa.dispWinFrames.length; ++i){pa.dispWinFrames[i].setGlobalTempoVal(tmpTempo);}}
//			break;}
		}			
	}//setUIWinVals
	@Override
	protected boolean hndlMouseMoveIndiv(int mouseX, int mouseY, myPoint mseClckInWorld){		return false;	}
	@Override
	protected boolean hndlMouseClickIndiv(int mouseX, int mouseY, myPoint mseClckInWorld, int mseBtn) {	
		if((!pa.ptInRange(mouseX, mouseY, rectDim[0], rectDim[1], rectDim[0]+rectDim[2], rectDim[1]+rectDim[3]))){return false;}//not in this window's bounds, quit asap for speedz
		int i = (int)((mouseY-(yOff + yOff + clkFlgsStY))/(yOff));					//TODO Awful - needs to be recalced, dependent on menu being on left
		if((i>=0) && (i<numMainFlagsToShow)){
			pa.setBaseFlag(mainFlagsToShow.get(i),!pa.getBaseFlag(mainFlagsToShow.get(i)));return true;	}
		else if(pa.ptInRange(mouseX, mouseY, 0, minBtnClkY, uiClkCoords[2], uiClkCoords[1])){
			boolean clkInBtnRegion = checkButtons(mouseX, mouseY);
			if(clkInBtnRegion) { this.setPrivFlags(mseClickedInBtnsIDX, true);}
			return clkInBtnRegion;
		}//in region where clickable buttons are - uiClkCoords[1] is bottom of buttons
		return false;
	}
	@Override
	protected boolean hndlMouseDragIndiv(int mouseX, int mouseY,int pmouseX, int pmouseY, myPoint mouseClickIn3D, myVector mseDragInWorld, int mseBtn) {//regular UI obj handling handled elsewhere - custom UI handling necessary to call main window		
		boolean res = pa.getCurFocusDispWindow().hndlMouseDragIndiv(mouseX, mouseY,pmouseX, pmouseY, mouseClickIn3D, mseDragInWorld, mseBtn);
		return res;	}
	@Override
	public void hndlMouseRelIndiv() {	clearAllBtnStates();}

	private void drawSideBarBooleans(){
		//draw main booleans and their state
		pa.translate(10,yOff*2);
		pa.setColorValFill(my_procApplet.gui_Black,255);
		pa.text("Boolean Flags",0,yOff*.20f);
		pa.translate(0,clkFlgsStY);
		for(int idx =0; idx<numMainFlagsToShow; ++idx){
			int i = mainFlagsToShow.get(idx);
			if(pa.getBaseFlag(i) ){												dispMenuTxtLat(truePFlagNames[i],pFlagColors[i], true);			}
			else {	if(truePFlagNames[i].equals(falsePFlagNames[i])) {		dispMenuTxtLat(truePFlagNames[i],new int[]{180,180,180}, false);}	
					else {													dispMenuTxtLat(falsePFlagNames[i],new int[]{0,255-pFlagColors[i][1],255-pFlagColors[i][2]}, true);}		
			}
		}	
	}//drawSideBarBooleans
	private void drawSideBarStateBools(){ //numStFlagsToShow
		pa.translate(110,10);
		float xTrans = (int)((pa.getMenuWidth()-100) / pa.numStFlagsToShow);
		for(int idx =0; idx<pa.numStFlagsToShow; ++idx){
			dispBoolStFlag(StateBoolNames[idx],stBoolFlagColors[idx], pa.getBaseFlag(pa.stateFlagsToShow.get(idx)),StrWdMult[idx]);			
			pa.translate(xTrans,0);
		}
	}
	
	//draw UI buttons that control functions, debug and global load/save stubs
	private void drawSideBarButtons(){
		pa.translate(xOff*.5f,(float)minBtnClkY);
		pa.setFill(new int[]{0,0,0}, 255);
		for(int row=0; row<guiBtnRowNames.length;++row){
			pa.text(guiBtnRowNames[row],0,-yOff*.15f);
			pa.translate(0,rowStYOff);
			float xWidthOffset = rectDim[2]/(1.0f * guiBtnNames[row].length), halfWay;
			pa.pushMatrix();pa.pushStyle();
			pa.strokeWeight(1.0f);
			pa.stroke(0,0,0,255);
			pa.noFill();
			pa.translate(-xOff*.5f, 0);
			for(int col =0; col<guiBtnNames[row].length;++col){
				halfWay = (xWidthOffset - pa.textWidth(guiBtnNames[row][col]))/2.0f;
				pa.setColorValFill(guiBtnStFillClr[guiBtnSt[row][col]+1],255);
				pa.rect(0,0,xWidthOffset, yOff);	
				pa.setColorValFill(guiBtnStTxtClr[guiBtnSt[row][col]+1],255);
				pa.text(guiBtnNames[row][col], halfWay, yOff*.75f);
				pa.translate(xWidthOffset, 0);
			}
			pa.popStyle();	pa.popMatrix();						
			pa.translate(0,btnLblYOff);
		}
	}//drawSideBarButtons	
	@Override
	protected final void drawOnScreenStuffPriv(float modAmtMillis) {}
	@Override//for windows to draw on screen
	protected final void drawRightSideInfoBarPriv(float modAmtMillis) {}
	@Override
	protected final void drawMe(float animTimeMod) {
		pa.pushMatrix();pa.pushStyle();
			drawSideBarBooleans();				//toggleable booleans 
		pa.popStyle();	pa.popMatrix();	
		pa.pushMatrix();pa.pushStyle();
			drawSideBarStateBools();				//lights that reflect various states
		pa.popStyle();	pa.popMatrix();	
		pa.pushMatrix();pa.pushStyle();			
			drawSideBarButtons();						//draw buttons
		pa.popStyle();	pa.popMatrix();	
		pa.pushMatrix();pa.pushStyle();
			drawGUIObjs();					//draw what global user-modifiable fields are currently available 
		pa.popStyle();	pa.popMatrix();			
		pa.pushMatrix();pa.pushStyle();
			pa.drawWindowGuiObjs();			//draw objects for window with primary focus
		pa.popStyle();	pa.popMatrix();	
	}
	
	@Override
	public final void drawCustMenuObjs(){}	
	//no custom camera handling for menu , float rx, float ry, float dz are all now member variables of every window
	@Override
	protected final void setCameraIndiv(float[] camVals){}
	@Override
	public void hndlFileLoad(File file, String[] vals, int[] stIdx) {
		hndlFileLoad_GUI(vals, stIdx);
	}
	@Override
	public ArrayList<String> hndlFileSave(File file) {
		ArrayList<String> res = hndlFileSave_GUI();

		return res;
	}
	@Override
	protected String[] getSaveFileDirNamesPriv() {return new String[]{"menuDir","menuFile"};	}
	@Override
	public void drawClickableBooleans() {	}//this is only for non-sidebar menu windows, to display their own personal buttons
	@Override
	protected final void snapMouseLocs(int oldMouseX, int oldMouseY, int[] newMouseLoc){}//not a snap-to window
	@Override
	protected final void setVisScreenDimsPriv() {}
	@Override
	protected myPoint getMsePtAs3DPt(myPoint mseLoc){return new myPoint(mseLoc.x,mseLoc.y,0);}
	@Override
	protected final void endShiftKeyI() {}
	@Override
	protected final void endAltKeyI() {}
	@Override
	protected final void endCntlKeyI() {}
	@Override
	protected final void closeMe() {}
	@Override
	protected final void showMe() {}
	@Override
	protected final void resizeMe(float scale) {}	
	@Override
	protected final void setCustMenuBtnNames() {}
	@Override
	protected boolean simMe(float modAmtSec) {return false;}
	@Override
	protected final void stopMe() {}
	@Override
	protected final void addSScrToWinIndiv(int newWinKey){}
	@Override
	protected final void addTrajToScrIndiv(int subScrKey, String newTrajKey){}
	@Override
	protected final void delSScrToWinIndiv(int idx) {}	
	@Override
	protected final void delTrajToScrIndiv(int subScrKey, String newTrajKey) {}		
	//no trajectory here
	@Override
	protected final void processTrajIndiv(myDrawnSmplTraj drawnTraj){}	
	@Override
	protected final void initDrwnTrajIndiv(){}
	@Override
	public String toString(){
		String res = super.toString();
		return res;
	}

}//mySideBarMenu