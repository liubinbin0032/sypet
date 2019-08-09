/*
 * Copyright (C) 2017 The SyPet Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.utexas.sypet.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import edu.utexas.hunter.model.CustomField;
import edu.utexas.hunter.model.CustomMethod;
import edu.utexas.hunter.model.CustomType;
import edu.utexas.sypet.Experiment;
import edu.utexas.sypet.synthesis.SyPetService;
import edu.utexas.sypet.synthesis.model.BinTree;
import edu.utexas.sypet.synthesis.model.Config;
import edu.utexas.sypet.synthesis.model.JGraph;
import edu.utexas.sypet.synthesis.model.Pair;
import edu.utexas.sypet.synthesis.model.Pent;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import uniol.apt.adt.exception.NoSuchEdgeException;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

public class SootUtil {

	// clone edges everywhere?
	protected static boolean clone = true;

	// Count the number of tokens? By default yes.
	protected static boolean countRes = true;

	public static Set<String> reachableTypes = new LinkedHashSet<>();

	public static Map<String, String> polyMap = new HashMap<>();

	protected static final JGraph graph = new JGraph();

	protected static Map<SootMethod, Set<String>> depMap = new HashMap<>();

	protected static Map<String, CustomMethod> hunterMap = new HashMap<>();

	protected static Map<String, Map<String, Integer>> consumeMap = new HashMap<>();

	// key: llTransition; Trio(s,t,f): s: src type, t: target type. f: intField
	public static Map<String, Pent<String, String, String, String, String>> llTransitions = new HashMap<>();

	public static Map<String, Pair<BinTree, BinTree>> BinTransitions = new HashMap<>();

	public static int classNum = 0;

	public static int methodNum = 0;

	private static final String UPPER = "_upper";

	private static Config cfg;
	
	public static Set<String> patternSet = new LinkedHashSet<>();
	public static Set<String> deprecatedSet = new HashSet<>();
	public static Map<String, List<String>> superDict = new HashMap<>();
	public static Map<String, List<String>> subDict = new HashMap<>();

	public static void initCfg() {
		try {
			JsonReader reader = new JsonReader(new FileReader("CONFIG.json"));
			Gson gson = new Gson();
			cfg = gson.fromJson(reader, Config.class);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// summarize # of tokens for each type in the argument.
	public static Map<String, Integer> sumTokens4Type(List<?> list) {
		Map<String, Integer> map = new HashMap<>();
		for (Object t : list) {
			String name = t.toString();
			if (map.containsKey(name)) {
				int cnt = map.get(name);
				cnt++;
				map.put(name, cnt);
			} else {
				map.put(name, 1);
			}
		}
		return map;
	}

	// Given a method's signature, return # of tokens per each argument type.
	public static Map<String, Integer> getArgConsumeById(String sig) {

		if (consumeMap.containsKey(sig))
			return consumeMap.get(sig);
		else
			return null;
	}

	public static List<String> getClones(List<String> list) {
		Map<String, Integer> map = new HashMap<>();
		List<String> clones = new ArrayList<>();
		for (String name : list) {
			if (map.containsKey(name)) {
				int cnt = map.get(name);
				cnt++;
				map.put(name, cnt);
			} else {
				map.put(name, 1);
			}
		}
		for (String type : map.keySet()) {
			for (int j = 1; j < map.get(type); j++) {
				clones.add("sypet_clone_" + type);
			}
		}
		return clones;
	}

	public static boolean isValidMeth(SootMethod meth) {
		if (cfg == null)
			initCfg();

		List<String> blacklist = cfg.getBlacklist();
		for (String black : blacklist) {
			if (meth.getSignature().contains(black)) {
				return false;
			}
		}

		if (meth.getParameterCount() > 4)
			return false;

		if (!meth.isConstructor() && !meth.isPublic())
			return false;

		return true;
	}

	public static void createPlace(PetriNet p, String pName) {
		if (!p.containsPlace(pName)) {
			p.createPlace(pName);
			// clone edges
			if (clone) {
				String cloneId = "sypet_clone_" + pName;
				createTransition(p, cloneId);
				p.createFlow(pName, cloneId);
				p.createFlow(cloneId, pName, 2);
			}
		}
	}

	public static void createTransition(PetriNet p, String pName) {
		if (!p.containsTransition(pName)) {
			p.createTransition(pName);
		}
	}

	// Checking the class is linkedlist.
	public static boolean isLinkedlist(SootClass clz) {
		int intCnt = 0;
		int self = 0;
		for (SootField sf : clz.getFields()) {
			Type t = sf.getType();
			if (t.toString().equals("int"))
				intCnt++;

			if (t.toString().equals(clz.getName()))
				self++;
		}
		return ((intCnt == 1) && (self == 1));
	}

	public static boolean isLinkedlist(CustomType clz) {
		int intCnt = 0;
		int self = 0;
		for (CustomField sf : clz.getFields()) {
			String t = sf.getType();
			if (t.toString().equals("int"))
				intCnt++;

			if (t.toString().equals(clz.getName()))
				self++;
		}
		return ((intCnt == 1) && (self == 1));
	}

	public static boolean isBinaryTree(CustomType clz) {
		int intCnt = 0;
		int self = 0;
		for (CustomField sf : clz.getFields()) {
			String t = sf.getType();
			if (t.toString().equals("int"))
				intCnt++;

			if (t.toString().equals(clz.getName()))
				self++;
		}
		return ((intCnt == 1) && (self == 2));
	}

	// create dummy pair-wise transitions.
	public static void createLinkedlistTransition(PetriNet p, Set<SootClass> classes) {

		for (SootClass src : classes) {
			for (SootClass tgt : classes) {
				if (!src.equals(tgt)) {
					createPlace(p, src.toString());
					createPlace(p, tgt.toString());
					String name = src.toString() + "_" + tgt.toString();
					createTransition(p, name);
					String intField = getIntField(src);
					String objField = getObjField(src);
					String objTgtField = getObjField(tgt);

					Pent<String, String, String, String, String> trio = new Pent<>(src.toString(), tgt.toString(),
							objField, objTgtField, intField);
					llTransitions.put(name, trio);
					p.createFlow(src.toString(), name);
					p.createFlow(name, tgt.toString());
				}
			}
		}
	}

	public static void createLinkedlistTransition(PetriNet p, List<CustomType> classes) {
		if (classes.size() < 2)
			return;

		for (CustomType src : classes) {
			for (CustomType tgt : classes) {
				if (!src.equals(tgt)) {
					String name = src.getName() + "_" + tgt.getName();
					if (p.containsTransition(name))
						continue;
					createPlace(p, src.getName());
					createPlace(p, tgt.getName());

					createTransition(p, name);
					String intField = getIntField(src);
					String objField = getObjField(src);
					String objTgtField = getObjField(tgt);

					Pent<String, String, String, String, String> trio = new Pent<>(src.getName(), tgt.getName(),
							objField, objTgtField, intField);
					llTransitions.put(name, trio);
					p.createFlow(src.getName(), name);
					p.createFlow(name, tgt.getName());
				}
			}
		}
	}

	public static void createBinTransition(PetriNet p, List<CustomType> classes) {
		if (classes.size() < 2)
			return;

		for (CustomType src : classes) {
			String idSrc = getIntField(src);
			Pair<String, String> nodesSrc = getlfNodes(src);
			BinTree binSrc = new BinTree(idSrc, nodesSrc.val0, nodesSrc.val1, src.getName());
			createPlace(p, src.getName());
			for (CustomType tgt : classes) {
				if (!src.equals(tgt)) {
					String name = src.getName() + "_" + tgt.getName();
					if (p.containsTransition(name))
						continue;
					createPlace(p, tgt.getName());
					createTransition(p, name);
					String idTgt = getIntField(tgt);
					Pair<String, String> nodesTgt = getlfNodes(tgt);
					BinTree binTgt = new BinTree(idTgt, nodesTgt.val0, nodesTgt.val1, tgt.getName());
					Pair<BinTree, BinTree> trio = new Pair<>(binSrc, binTgt);
					BinTransitions.put(name, trio);
					p.createFlow(src.getName(), name);
					p.createFlow(name, tgt.getName());
				}
			}
		}
	}

	public static String getIntField(SootClass sc) {
		String str = "";
		for (SootField sf : sc.getFields()) {
			Type t = sf.getType();
			if (t.toString().equals("int")) {
				str = sf.getName();
				break;
			}
		}
		return str;
	}

	public static String getObjField(SootClass sc) {
		String str = "";
		for (SootField sf : sc.getFields()) {
			Type t = sf.getType();
			if (sc.getName().equals(t.toString())) {
				str = sf.getName();
				break;
			}
		}
		return str;
	}

	public static String getObjField(CustomType sc) {
		String str = "";
		for (CustomField sf : sc.getFields()) {
			String t = sf.getType();
			if (sc.getName().equals(t.toString())) {
				str = sf.getName();
				break;
			}
		}
		return str;
	}

	// get left & right nodes of a binary tree.
	public static Pair<String, String> getlfNodes(CustomType sc) {
		List<String> children = new ArrayList<>();
		for (CustomField sf : sc.getFields()) {
			String t = sf.getType();
			if (sc.getName().equals(t.toString())) {
				children.add(sf.getName());
			}
		}
		// order left/right by alphabetical order.
		java.util.Collections.sort(children);
		assert children.size() == 2;
		String v0 = children.get(0);
		String v1 = children.get(1);
		return new Pair<>(v0, v1);
	}

	public static String getIntField(CustomType sc) {
		String str = "";
		for (CustomField sf : sc.getFields()) {
			String t = sf.getType();
			if (t.toString().equals("int")) {
				str = sf.getName();
				break;
			}
		}
		return str;
	}

	// package of certain jar.
	public static void processJar(String jarPath, Set<String> pkg, PetriNet p, Map<String, Set<String>> superClassMap) {
		pkg.addAll(getBuildinPkg());
		createPlace(p, "void");
		Set<SootClass> linkedlists = new HashSet<>();
		for (String cl : SourceLocator.v().getClassesUnder(jarPath)) {
			SootClass clazz = Scene.v().getSootClass(cl);
			boolean skip = true;
			for (String pName : pkg) {
				if (cl.startsWith(pName)) {
					skip = false;
					break;
				}
			}

			if (skip)
				continue;

			if (!clazz.isPublic()) {
				continue;
			}
			
			if (clazz.toString().contains("java.awt.geom.Path2D"))
				continue;
			
			classNum++;
			if (isLinkedlist(clazz)) {
				linkedlists.add(clazz);
			}

			LinkedList<SootMethod> methodsCopy = new LinkedList<SootMethod>(clazz.getMethods());
			for (SootMethod meth : methodsCopy) {
				
				if (clazz.isAbstract() && meth.getName().equals("<init>")) {
//					System.out.println("abstract: "+meth );
					continue;
				}
				
				
				String formMethod = getMethodForm(meth.toString());
				if (deprecatedSet.contains(formMethod)) {
//					System.out.println("deprecated " + formMethod);
					continue;
				}

				methodNum++;
				if (!isValidMeth(meth))
					continue;

				if (meth.isPublic() || meth.isStatic()) {

					String signature = meth.getSignature();
					String retName = meth.getReturnType().toString();

					boolean redirect = redirectFlow(cl, meth, retName, p);
					if (redirect)
						continue;

					LinkedList<Type> ll = new LinkedList<>();

					if (!meth.isStatic() && !meth.isConstructor()) {
						ll.add(clazz.getType());
					}
					ll.addAll(meth.getParameterTypes());

					createTransition(p, signature);
					createPlace(p, retName);
					Set<String> inputTypes = new LinkedHashSet<>();

					for (Type t : ll) {
						String pname = t.toString();
						// pname = substitute(pname, signature);
						inputTypes.add(pname);
						createPlace(p, pname);
					}

					// add flows.
					Map<String, Integer> map = sumTokens4Type(ll);
					consumeMap.put(signature, map);
					// arguments.
					for (String type : map.keySet()) {
						int cnt = countRes ? map.get(type) : 1;
						// type = substitute(type, signature);
						p.createFlow(type, signature, cnt);
					}
					// return.
					if (meth.isConstructor()) {
						String clzName = clazz.getName();
						createPlace(p, clzName);
						retName = clzName;
					}
					p.createFlow(signature, retName);
				}
			}

		}

		getPolymorphismInformation(p, superClassMap);
		createLinkedlistTransition(p, linkedlists);
	}

	public static void updateReachableTypes(String tgtType, int val) {
		// set bound
		graph.setK(val);
		reachableTypes = graph.backwardReach2(tgtType);
	}

	// modify flow dues to inheritance.
	public static boolean redirectFlow(String cl, SootMethod meth, String retName, PetriNet p) {
		if (meth.isStatic() && meth.getParameterCount() == 0) {
			// from void to type.
			createTransition(p, meth.getSignature());
			createPlace(p, "void");
			createPlace(p, retName);
			p.createFlow("void", meth.getSignature());
			p.createFlow(meth.getSignature(), retName);
			return true;
		}

		// handle empty constructor.
		if (meth.isConstructor() && (meth.getParameterCount() == 0)) {
			// from void to type.
			createTransition(p, meth.getSignature());
			createPlace(p, "void");
			createPlace(p, cl);
			p.createFlow("void", meth.getSignature());
			p.createFlow(meth.getSignature(), cl);
			return true;
		}

		return false;
	}

	public static boolean isHunterMethod(String m) {
		return hunterMap.containsKey(m);
	}

	public static CustomMethod getHunterMethod(String m) {
		return hunterMap.get(m);
	}

	public static CustomMethod addHunterMethod(String m, CustomMethod meth) {
		return hunterMap.put(m, meth);
	}

	public static void setClone(boolean flag) {
		clone = flag;
	}

	public static void setCount(boolean flag) {
		countRes = flag;
	}

	// Generate compact PetriNet for Ruben.
	public static PetriNet getCompactGraph(PetriNet srcNet) {
		// /Compute max of each type.
		// init map.
		Map<String, Integer> maxTokenMap = new HashMap<>();
		for (Place p : srcNet.getPlaces()) {
			maxTokenMap.put(p.getId(), 1);
		}
		// compute max.
		for (Transition t : srcNet.getTransitions()) {
			for (Flow flow : t.getPresetEdges()) {
				String argType = flow.getSource().getId();
				int num = flow.getWeight();
				assert maxTokenMap.containsKey(argType);
				int curr = maxTokenMap.get(argType);
				if (num > curr)
					maxTokenMap.put(argType, num);
			}
		}

		// construct new PetriNet.
		PetriNet tgtNet = new PetriNet();
		// Create place first.
		for (String type : maxTokenMap.keySet()) {
			int num = maxTokenMap.get(type);
			List<String> cloneArgs = new ArrayList<>();
			for (int i = 0; i < num; i++) {
				String typeClone = type + "_" + i;
				tgtNet.createPlace(typeClone);
				cloneArgs.add(typeClone);
			}
			// Add reuse transition.
			int reuseCnt = 0;
			for (String src : cloneArgs) {
				for (String tgt : cloneArgs) {
					if (src.equals(tgt))
						continue;

					String reuse = "reuse_" + type + "_" + reuseCnt;
					tgtNet.createTransition(reuse);
					tgtNet.createFlow(src, reuse);
					tgtNet.createFlow(reuse, tgt);
					reuseCnt++;
				}
			}
		}

		// Create transition and flow
		for (Transition t : srcNet.getTransitions()) {
			assert t.getPostset().size() == 1;
			Place ret = t.getPostset().iterator().next();
			String retStr = ret.getId();
			// / clone this transition based on the # of clone.
			int cloneNum = maxTokenMap.get(retStr);
			for (int i = 0; i < cloneNum; i++) {
				String transitionClone = t.getId() + "_" + i;
				String retClone = retStr + "_" + i;
				// outgoing edge.
				tgtNet.createTransition(transitionClone);
				tgtNet.createFlow(transitionClone, retClone);
				// incoming edges.
				for (Flow flow : t.getPresetEdges()) {
					String argType = flow.getSource().getId();
					int tokens = flow.getWeight();
					for (int idx = 0; idx < tokens; idx++) {
						String argClone = argType + "_" + idx;
						assert tgtNet.containsPlace(argClone) : argClone;
						tgtNet.createFlow(argClone, transitionClone);
					}
				}
			}
		}

		return tgtNet;
	}

	public static void handlePolymorphism(PetriNet p) {

		assert cfg != null;
		List<String> polyList = cfg.getPoly();
		int upperCnt = 0;
		for (String raw : polyList) {
			// left is the subclass of rt. i.e., rt <= left.
			String left = raw.split(",")[0];
			String rt = raw.split(",")[1];
			if (!p.containsNode(left))
				createPlace(p, left);
			String tranName = UPPER + upperCnt;
			createTransition(p, tranName);
			if (!p.containsNode(rt))
				createPlace(p, rt);

			p.createFlow(left, tranName);
			p.createFlow(tranName, rt);
			SyPetService.sdkTypes.put(tranName, new Pair<>(left, rt));
			upperCnt++;
		}
	}

	public static Set<String> getBuildinPkg() {
		if (cfg == null)
			initCfg();

		Set<String> mySet = new HashSet<>(cfg.getBuildinPkg());
		return mySet;
	}
	
	public static void handlePattern(PetriNet p, List<String> ptnList) {
		
		for (String pattern : ptnList) {
			ArrayList<String> seqList = new ArrayList<>(Arrays.asList(pattern.split(", ")));
			// System.out.println(seqList.size());
			
			ArrayList<ArrayList<String>> transGroupList = new ArrayList<>();
 			for (String seq : seqList) {
 				ArrayList<String> transGroup = new ArrayList<>();
 				for (Transition t : p.getTransitions()) {
 					if (patternSet.contains(t.getId()))
 						continue;
 					String class_name = seq.substring(0, seq.lastIndexOf("."));
 					String method_name = seq.substring(seq.lastIndexOf(".")+1);
 					if (t.getId().contains(class_name) && t.getId().contains(method_name + "(")) {
						transGroup.add(t.getId());
					}
 				}
 				transGroupList.add(transGroup);
			}
 			// System.out.println(transGroupList.size());

 			// create short path for each 2 transitions
 			for (int i = 0; i < transGroupList.size(); i++) 
 				for (int j = i+1; j < transGroupList.size(); j++) 
 					for (String trans1 : transGroupList.get(i)) 
 	 					for (String trans2: transGroupList.get(j)) {
 	 						Transition t1 = p.getTransition(trans1);		
 	 						Transition t2 = p.getTransition(trans2);
 	 						
 	 						Set<Place> t2prePlaces = t2.getPreset();
 	 						Place t1post = t1.getPostset().iterator().next();
 	 						if (t2prePlaces.contains(t1post)) {
 	 							Experiment.pathList.add(pattern);
 	 							if (Experiment.PATTERN)
 	 								createPattern2(p, trans1, trans2);
 	 						}
 	 					}

 			// create short path for each 3 transitions
 			if (transGroupList.size() > 2) 
	 			for (int i = 0; i < transGroupList.size(); i++) 
	 				for (int j = i+1; j < transGroupList.size(); j++) 
	 					for (int k = j+1; k < transGroupList.size(); k++) 
	 						for (String trans1 : transGroupList.get(i)) 
		 	 					for (String trans2: transGroupList.get(j)) 
		 	 						for (String trans3: transGroupList.get(k)) {
		 	 							Transition t1 = p.getTransition(trans1);		
		 	 							Transition t2 = p.getTransition(trans2);
		 	 							Transition t3 = p.getTransition(trans3);
		 	 							
		 	 							Set<Place> t2prePlaces = t2.getPreset();
		 	 							Place t1post = t1.getPostset().iterator().next();
		 	 							
		 	 							Set<Place> t3prePlaces = t3.getPreset();
		 	 							Place t2post = t2.getPostset().iterator().next();
		 	 							
		 	 							if (t2prePlaces.contains(t1post) && t3prePlaces.contains(t2post)) {
		 	 								if (Experiment.PATTERN)
		 	 									createPattern3(p, trans1, trans2, trans3);	
		 	 							}
		 	 						}				
		}

		List<String> consList = new ArrayList<>();
		for (String pattern : Experiment.ptnList) {
			if (Experiment.pathList.contains(pattern))
				continue;
			
			boolean flag = false;
			for (String item : consList) {
				if (Arrays.asList(item.split(", ")).containsAll(Arrays.asList(pattern.split(", ")))) {
					flag = true;
					break;
				} else if (Arrays.asList(pattern.split(", ")).containsAll(Arrays.asList(item.split(", "))))
					consList.remove(item);
			}
			
			if (flag)
				continue;

			consList.add(pattern);
		}
		Experiment.consList = consList;
	}
	
	public static boolean createPattern2(PetriNet p, String trans1, String trans2) {
		Transition t1 = p.getTransition(trans1);		
		Transition t2 = p.getTransition(trans2);
		
		Set<Place> t2prePlaces = t2.getPreset();
		Iterator<Place> it2 = t2prePlaces.iterator();
		Place t1post = t1.getPostset().iterator().next();

		String seqName = t1.getId() + "<-" + t1post.getId() + "->" + t2.getId();
		if (p.containsTransition(seqName))
			return false;
		createTransition(p, seqName);
		
		Set<Place> t1prePlaces = t1.getPreset();
		Iterator<Place> it1 = t1prePlaces.iterator();
		// connect t1pre to seq
		while (it1.hasNext()) {
			Place t1pre = it1.next();
			int weight = p.getFlow(t1pre.getId(), t1.getId()).getWeight();
			addFlow(p, t1pre.getId(), seqName, weight);
		}
		
		// connect t2pre to seq
		while (it2.hasNext()) {
			Place t2pre = it2.next();
			// t2pre in t1prePlaces
			if (t1prePlaces.contains(t2pre)) {
				int weight = p.getFlow(t2pre.getId(), t2.getId()).getWeight();
				if (t1post.equals(t2pre))
					addFlow(p, t2pre.getId(), seqName, weight - 1);
				else
					addFlow(p, t2pre.getId(), seqName, weight);
			} else {
				if (t2pre.equals(t1post)) {
					int weight = p.getFlow(t2pre.getId(), t2.getId()).getWeight();
					if (weight == 1)
						continue;
					addFlow(p, t2pre.getId(), seqName, weight - 1);
				} else {
					int weight = p.getFlow(t2pre.getId(), t2.getId()).getWeight();
					addFlow(p, t2pre.getId(), seqName, weight);
				}
			}
		}
		Place t2post = t2.getPostset().iterator().next();
		addFlow(p, seqName, t2post.getId(), 1);
		patternSet.add(seqName);
		return true;
	}
	
	public static boolean createPattern3(PetriNet p, String trans1, String trans2, String trans3) {
		Transition t1 = p.getTransition(trans1);		
		Transition t2 = p.getTransition(trans2);
		Transition t3 = p.getTransition(trans3);
		
		Set<Place> t2prePlaces = t2.getPreset();
		Iterator<Place> it2 = t2prePlaces.iterator();
		Place t1post = t1.getPostset().iterator().next();
		
		Set<Place> t3prePlaces = t3.getPreset();
		Iterator<Place> it3 = t3prePlaces.iterator();
		Place t2post = t2.getPostset().iterator().next();
		
		String seqName = t1.getId() + "<-" + t1post.getId() + "->" + t2.getId() + "<-" + t2post.getId() + "->" + t3.getId();
		if (p.containsTransition(seqName))
			return false;
		createTransition(p, seqName);
		
		Set<Place> t1prePlaces = t1.getPreset();
		Iterator<Place> it1 = t1prePlaces.iterator();
		while (it1.hasNext()) {
			Place t1pre = it1.next();		
			int weight = p.getFlow(t1pre.getId(), t1.getId()).getWeight();
			addFlow(p, t1pre.getId(), seqName, weight);
		}
		
		while (it2.hasNext()) {
			Place t2pre = it2.next();
			// t2pre in t1prePlaces
			if (t1prePlaces.contains(t2pre)) {
				int weight = p.getFlow(t2pre.getId(), t2.getId()).getWeight();
				if (t1post.equals(t2pre))
					addFlow(p, t2pre.getId(), seqName, weight - 1);
				else
					addFlow(p, t2pre.getId(), seqName, weight);
			} else {
				if (t1post.equals(t2pre)) {
					int weight = p.getFlow(t2pre.getId(), t2.getId()).getWeight();
					if ( weight == 1)
						continue;
					addFlow(p, t2pre.getId(), seqName, weight - 1);
				} else {
					int weight = p.getFlow(t2pre.getId(), t2.getId()).getWeight();
					addFlow(p, t2pre.getId(), seqName, weight);	
				}
			}
		}

		while (it3.hasNext()) {
			Place t3pre = it3.next();
			if (t1prePlaces.contains(t3pre)) {
				int weight = p.getFlow(t3pre.getId(), t3.getId()).getWeight();
				if (t2post.equals(t3pre))
					addFlow(p, t3pre.getId(), seqName, weight - 1);
				else
					addFlow(p, t3pre.getId(), seqName, weight);
			} else if (t2prePlaces.contains(t3pre)) {
				int weight = p.getFlow(t3pre.getId(), t3.getId()).getWeight();
				if (t2post.equals(t3pre))
					addFlow(p, t3pre.getId(), seqName, weight - 1);
				else
					addFlow(p, t3pre.getId(), seqName, weight);
			} else {	
				if (t3pre.equals(t2post)) {
					int weight = p.getFlow(t3pre.getId(), t3.getId()).getWeight();
					if ( weight == 1 )
						continue;
					addFlow(p, t3pre.getId(), seqName, weight - 1);
				} else {
					int weight = p.getFlow(t3pre.getId(), t3.getId()).getWeight();
					addFlow(p, t3pre.getId(), seqName, weight);	
				}
			}
		}
		Place t3post = t3.getPostset().iterator().next();
		addFlow(p, seqName, t3post.getId(), 1);
		// System.out.println("pattern: " + seqName);
		patternSet.add(seqName);	
		return true;
	}
	
	public static void addFlow(PetriNet petrinet, String ID1, String ID2, int weight) {
		Flow f;
		try {
			f = petrinet.getFlow(ID1, ID2);
			f.setWeight(f.getWeight() + weight);
		} catch (NoSuchEdgeException e) {
			petrinet.createFlow(ID1, ID2, weight);
		}
	}
	
	public static String getMethodForm(String signature) {
		String className = signature.substring(1, signature.indexOf(":"));
		className = className.substring(className.lastIndexOf(".")+1, className.length());
		String methodName = signature.substring(signature.indexOf(":")+2, signature.indexOf("("));
		methodName = methodName.substring(methodName.indexOf(" ")+1, methodName.length());
		String paraName = signature.substring(signature.indexOf("(")+1, signature.indexOf(")"));
		String paraNew = "";
		if (paraName.contains(",")) {
			for (String para: paraName.split(",")) {
				para = para.substring(para.lastIndexOf(".")+1, para.length());
				paraNew = paraNew.concat(para).concat(",");
			}
			paraNew = paraNew.substring(0, paraNew.length()-1);
		}
		else 
			paraNew = paraName.substring(paraName.lastIndexOf(".")+1, paraName.length());
		String name = className + "." + methodName + "(" + paraNew + ")";
		return name;
	}
	
	public static void copyPolymorphism(PetriNet petrinet) {
		// Handles polymorphism by creating copies for each method that has superclass as input type
		for (Transition t : petrinet.getTransitions()) {
			if (!t.getId().contains("<") || t.getId().contains("sypet_clone"))
				continue;
			
			String transName = t.getId();
//			System.out.println("Trans " + transName);
			String delClass = transName.substring(1, transName.indexOf(":"));
			String paraList = transName.substring(transName.indexOf("(") + 1, (transName.indexOf(")")));
			
			List<Place> inputs = new ArrayList<>();
			Set<Flow> inEdges = t.getPresetEdges();
			for (Flow f : inEdges) {
				if (f.getPlace().getId().equals(delClass)) {							
					inputs.add(f.getPlace());
//					System.out.println("Place" + f.getPlace());
					break;
				}
			}
			if (!paraList.equals("")) {
				for (String para : paraList.split(",")) {
					for (Flow f : inEdges) {
						if (f.getPlace().getId().equals(para)) {	
							inputs.add(f.getPlace());
//							System.out.println("Place" + f.getPlace());
						}
					}
				}
			}
			
			Stack<Place> polyInputs = new Stack<>();
			generatePolymophism(petrinet, t, 0, inputs, polyInputs);
		}
	}
	
	private static void generatePolymophism(PetriNet petrinet, Transition t, int count, List<Place> inputs, Stack<Place> polyInputs) {
		if (inputs.size() == count) {
			boolean skip = true;
			for (int i = 0; i < inputs.size(); i++) {
				if (!inputs.get(i).equals(polyInputs.get(i))) {
					skip = false;
				}
			}
			if (skip)
				return;

			String newTransitionName = t.getId() + "Poly:(";
			for (Place p : polyInputs) {
				newTransitionName += p.getId() + " ";
			}
			newTransitionName += ")";

			if (petrinet.containsTransition(newTransitionName)) {
				return;
			}

			boolean polymorphicOutput = false;
			for (Flow f : t.getPostsetEdges()) {
				Place p = f.getPlace();
				List<String> subClasses = superDict.get(p.getId());
				if (subClasses != null){
					polymorphicOutput = true;
					break;
				}
				if (polymorphicOutput)
					break;
			}

			Transition newTransition = petrinet.createTransition(newTransitionName);
			for (Place p : polyInputs) {
				// NOTE: why is the weight of the flow restricted to 1?
				addFlow(petrinet, p.getId(), newTransitionName, 1);
			}

			for (Flow f : t.getPostsetEdges()) {
				Place p = f.getPlace();
				int w = f.getWeight();
				petrinet.createFlow(newTransition, p, w);
			}
			//System.out.println(newTransitionName);
			polyMap.put(newTransitionName, t.getId());

			if (polymorphicOutput){

				for (Flow f : t.getPostsetEdges()) {
					Place p = f.getPlace();
					List<String> subClasses = superDict.get(p.getId());
					for (String s : subClasses) {
						if (!petrinet.containsPlace(s))
							continue;
						String newPolyTransitionName = newTransitionName+"(" + s + ")";
						assert (!petrinet.containsTransition(newPolyTransitionName));
						newTransition = petrinet.createTransition(newPolyTransitionName);
						for (Place p2 : polyInputs) {
							addFlow(petrinet, p2.getId(), newPolyTransitionName, 1);
						}
						int w = f.getWeight();
						petrinet.createFlow(newTransition, petrinet.getPlace(s), w);
//						System.out.println(newPolyTransitionName);
						polyMap.put(newPolyTransitionName, t.getId());
					}
				}

			}

		} else {
			Place p = inputs.get(count);
			List<String> subClasses = subDict.get(p.getId());
			if (subClasses == null) { // No possible polymophism
				polyInputs.push(p);
				generatePolymophism(petrinet, t, count + 1, inputs, polyInputs);
				polyInputs.pop();
				return;
			} else {
				for (String subclass : subClasses) {
					createPlace(petrinet, subclass);			
//					addPlace(subclass);
					Place polyClass = petrinet.getPlace(subclass);
					polyInputs.push(polyClass);
					generatePolymophism(petrinet, t, count + 1, inputs, polyInputs);
					polyInputs.pop();
				}
				return;
			}
		}
	}
	
	private static void getPolymorphismInformation(PetriNet petrinet, Map<String, Set<String>> superClassMap) {
		
		Map<String, Set<String>> subClassMap = new HashMap<>();
		for (String key : superClassMap.keySet()) {
			for (String value : superClassMap.get(key)) {
				if (!subClassMap.containsKey(value)) {
					subClassMap.put(value, new HashSet<String>());
				}
				subClassMap.get(value).add(key);
			}
		}
		
		for (String s : superClassMap.keySet()) {
			if (!petrinet.containsPlace(s))
				continue;

			Set<String> superClasses = superClassMap.get(s);
			if (superClasses.size() != 0) {
				List<String> superClassList = new ArrayList<>(superClasses);
				superDict.put(s, superClassList);
			}
		}
		for (String s : subClassMap.keySet()) {
			if (!petrinet.containsPlace(s))
				continue;
			
			Set<String> subClasses = subClassMap.get(s);
			if (subClasses.size() != 0) {
				List<String> subClassList = new ArrayList<>(subClasses);
				subDict.put(s, subClassList);
			}
		}
	}
}
