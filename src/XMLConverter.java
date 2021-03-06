import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLConverter {
	
	private static final String PREFAB_BASE_ADDRESS = "src/resourses/prefabsbase.xml";
	
	private static final String PREFAB_CATEGORY_BASE_ADDRESS = "src/resourses/prefabcategorybase.xml";
	
	private static final String ATLAS_BASE_ADDRESS = "src/resourses/atlasesbase.xml";
	
	private File fXmlFile;
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;
	
	public XMLConverter(){
		try{
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
		}
		catch(Exception ex){
			System.out.println(ex.toString());
		}
	}
	
	//Returns list of prefabs currently existing in prefabsbase.xml file
	public ArrayList<Prefab> loadPrefabBase(){
		try {
			fXmlFile = new File(PREFAB_BASE_ADDRESS);
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			ArrayList<Prefab> prefabs = new ArrayList<Prefab>();

			NodeList nList = doc.getElementsByTagName("prefab");
					
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String ID = eElement.getAttribute("id");
					String categoryID = eElement.getAttribute("category");
					int tiledWidth = Integer.parseInt(eElement.getAttribute("tiledwidth"));
					int tiledHeight = Integer.parseInt(eElement.getAttribute("tiledheight"));
					Element textureElement = (Element)eElement.getElementsByTagName("texture").item(0);
					String assetName = textureElement.getAttribute("asset");
					Asset asset = GOBase.assetsBase.get(assetName);
					String description= eElement.getElementsByTagName("description").item(0).getTextContent();
					
					ArrayList<AdditiveAttribute> additiveAttributes = new ArrayList<AdditiveAttribute>();
					NodeList nList2 = eElement.getElementsByTagName("additiveattribute");
					for (int temp2 = 0; temp2 < nList2.getLength(); temp2++) {
						Node nNode2 = nList2.item(temp2);
						if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement2 = (Element) nNode2;
							String aaName = eElement2.getAttribute("name");
							String aaType = eElement2.getAttribute("type");
							String aaValue = eElement2.getAttribute("value");
							additiveAttributes.add(new AdditiveAttribute(aaName,aaType,aaValue));
						}
					}
					
					prefabs.add(new Prefab(ID,categoryID,tiledWidth,tiledHeight,asset,description,additiveAttributes));
				}
			}
			//Setting up links
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if(!eElement.getAttribute("link").isEmpty()){
						String link = eElement.getAttribute("link");
						Prefab slavePrefab = null;
						for(Prefab p : prefabs)
							if(p.getPrefabID().equals(link))
								slavePrefab = p;
						
						prefabs.get(temp).setSlavePrefab(slavePrefab);
						slavePrefab.setMasterPrefab(prefabs.get(temp));
					}
				}
			}
			return prefabs;
		    } catch (Exception e) {
			e.printStackTrace();
			return null;
		    }
	}
	
	public boolean savePrefabBase(){
		try {
			// root elements
			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("prefabsbase");
			doc.appendChild(rootElement);

			for(Prefab P : GOBase.prefabsBase){;
				Element prefab = doc.createElement("prefab");
				rootElement.appendChild(prefab);

				// set attribute to staff element
				prefab.setAttribute("id", P.getPrefabID());
				prefab.setAttribute("category", P.getCategoryID());
				prefab.setAttribute("tiledwidth", String.valueOf(P.getTiledWidth()));
				prefab.setAttribute("tiledheight", String.valueOf(P.getTiledHeight()));
				if(P.isMaster())
					prefab.setAttribute("link", P.getSlavePrefab().getPrefabID());
				
				Element texture = doc.createElement("texture");
				texture.setAttribute("atlas", P.getAtlasName());
				texture.setAttribute("asset", P.getAssetName());
				prefab.appendChild(texture);
				
				Element description = doc.createElement("description");
				description.appendChild(doc.createTextNode(P.getDesctiption()));
				prefab.appendChild(description);
				
				for(AdditiveAttribute a : P.getAdditiveAttributes()){
					Element additiveAttribute = doc.createElement("additiveattribute");
					additiveAttribute.setAttribute("name", a.getAttributeName().toLowerCase());
					additiveAttribute.setAttribute("type", a.getAttributeType());
					additiveAttribute.setAttribute("value", a.getAttributeValue());
					prefab.appendChild(additiveAttribute);
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(PREFAB_BASE_ADDRESS));
			
			transformer.transform(source, result);
			
			return true;
		} 
		catch (Exception ex) {
			  ex.printStackTrace();
			  
			  return false;
		}
	}

	public ArrayList<PrefabCategory> loadPrefabCategoryBase(){
		try {
			fXmlFile = new File(PREFAB_CATEGORY_BASE_ADDRESS);
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			ArrayList<PrefabCategory> prefabCategoryBase = new ArrayList<PrefabCategory>();

			NodeList nList = doc.getElementsByTagName("prefabcategory");
					
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					
					Element eElement = (Element) nNode;
					String ID = eElement.getAttribute("id");
					String name = eElement.getAttribute("name");
					boolean isObstacle = Boolean.valueOf(eElement.getAttribute("isobstacle"));
					
					ArrayList<AdditiveAttribute> aaList = new ArrayList<AdditiveAttribute>();
					NodeList nList2 = eElement.getElementsByTagName("additiveattribute");
					for(int i=0; i<nList2.getLength();i++){
						Node nNode2 = nList2.item(i);
						if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement2 = (Element) nNode2;
							String aaName = eElement2.getAttribute("name");
							String aaType = eElement2.getAttribute("type");
							String aaValue = eElement2.getAttribute("defaultvalue");
							
							aaList.add(new AdditiveAttribute(aaName,aaType,aaValue));
						}
					}
					
					prefabCategoryBase.add(new PrefabCategory(ID,name,isObstacle,aaList));
				}
			}
			return prefabCategoryBase;
		    } catch (Exception e) {
			e.printStackTrace();
			return null;
		    }
	}
	
	public boolean savePrefabCategoryBase(){
		try {
			// root elements
			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("prefabcategorybase");
			doc.appendChild(rootElement);

			for(PrefabCategory P : GOBase.prefabCategoryBase){
				Element prefabCategory = doc.createElement("prefabcategory");
				rootElement.appendChild(prefabCategory);

				prefabCategory.setAttribute("id", P.getID());
				prefabCategory.setAttribute("name", P.getName());
				prefabCategory.setAttribute("isObstacle", String.valueOf(P.getObstacleBit()));
				for(AdditiveAttribute a : P.getAdditiveAttributes()){
					Element additiveAttribute = doc.createElement("additiveattribute");
					additiveAttribute.setAttribute("name", a.getAttributeName().toLowerCase());
					additiveAttribute.setAttribute("type", a.getAttributeType());
					additiveAttribute.setAttribute("defaultvalue", a.getAttributeValue());
					prefabCategory.appendChild(additiveAttribute);
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(PREFAB_CATEGORY_BASE_ADDRESS));

			transformer.transform(source, result);

			return true;
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public boolean saveLevel(String fileAddress){
		try {
			Level level = ConstructorWindow.globals.level;
			// root elements
			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("level");
			rootElement.setAttribute("width", String.valueOf(level.getWidth()));
			rootElement.setAttribute("height", String.valueOf(level.getHeight()));
			rootElement.setAttribute("defaultwidth", String.valueOf(level.getDefaultWidth()));
			rootElement.setAttribute("defaultheight", String.valueOf(level.getDefaultHeight()));
			
			doc.appendChild(rootElement);
			
			Element objectsElement = doc.createElement("gameobjects");
			rootElement.appendChild(objectsElement);
			
			for(GameObject go : ConstructorWindow.globals.level.getObjects()){
				Element gameObject = doc.createElement("gameobject");
				objectsElement.appendChild(gameObject);
				
				gameObject.setAttribute("id", String.valueOf(go.getIndex()));
				gameObject.setAttribute("category", go.getPrefab().getCategoryID());
				gameObject.setAttribute("prefab", go.getPrefab().getPrefabID());
				gameObject.setAttribute("posx", String.valueOf(go.getPosition().x));
				gameObject.setAttribute("posy", String.valueOf(go.getPosition().y));
				gameObject.setAttribute("width", String.valueOf(go.getTiledWidth()));
				gameObject.setAttribute("height", String.valueOf(go.getTiledHeight()));
				gameObject.setAttribute("texture", go.getPrefab().getAssetName());
				if(go.isMaster())
					gameObject.setAttribute("link", String.valueOf(go.getSlave().getIndex()));
				
				for(AdditiveAttribute a : go.getPrefab().getAdditiveAttributes()){
					Element additiveAttribute = doc.createElement("additiveattribute");
					additiveAttribute.setAttribute("name", a.getAttributeName().toLowerCase());
					additiveAttribute.setAttribute("type", a.getAttributeType());
					additiveAttribute.setAttribute("value", a.getAttributeValue());
					gameObject.appendChild(additiveAttribute);
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileAddress));

			transformer.transform(source, result);
			level.setFileAddress(fileAddress);
			return true;
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public Level loadLevel(String fileAddress){
		try {
			fXmlFile = new File(fileAddress);
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			Element levelElement = (Element)doc.getElementsByTagName("level").item(0);
			int defaultWidth = Integer.valueOf(levelElement.getAttribute("defaultwidth"));
			int defaultHeight = Integer.valueOf(levelElement.getAttribute("defaultheight"));
			int width = Integer.valueOf(levelElement.getAttribute("width"));
			int height = Integer.valueOf(levelElement.getAttribute("height"));
			
			Level level=new Level(defaultWidth,defaultHeight);
			level.setSize(width,height);
			
			ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
			
			Element objectsElement = (Element)levelElement.getElementsByTagName("gameobjects").item(0);
			NodeList nList = objectsElement.getElementsByTagName("gameobject");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					int index = Integer.valueOf(eElement.getAttribute("id"));
					Prefab prefab = GOBase.prefabsBase.get(eElement.getAttribute("prefab"));
					int posX = Integer.valueOf(eElement.getAttribute("posx"));
					int posY = Integer.valueOf(eElement.getAttribute("posy"));
					
					GameObject gameObject = new GameObject(prefab,posX,posY);
					gameObject.setIndex(index);
					gameObjects.add(gameObject);
				}
			}
			//Setting up links
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if(!eElement.getAttribute("link").isEmpty()){
						int link = Integer.valueOf(eElement.getAttribute("link"));
						GameObject slaveObject = null;
						for(GameObject go : gameObjects)
							if(go.getIndex()==link)
								slaveObject = go;
						
						gameObjects.get(temp).setSlave(slaveObject);
						slaveObject.setMaster(gameObjects.get(temp));
					}
				}
			}
			
			level.setObjects(gameObjects);
			level.setFileAddress(fileAddress);
			return level;
		    } catch (Exception e) {
			e.printStackTrace();
			return null;
		    }
	}
	
	public ArrayList<Atlas> loadAtlasesBase(){
		try {
			fXmlFile = new File(ATLAS_BASE_ADDRESS);
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			ArrayList<Atlas> atlasesBase = new ArrayList<Atlas>();

			NodeList atlasNList = doc.getElementsByTagName("atlas");
					
			for (int temp = 0; temp < atlasNList.getLength(); temp++) {
				Node atlasNode = atlasNList.item(temp);
				if (atlasNode.getNodeType() == Node.ELEMENT_NODE) {
					Element atlasElement = (Element) atlasNode;
					
					String atlasName = atlasElement.getAttribute("name");
					
					Atlas atlas = new Atlas(atlasName);
					
					NodeList assetNList = atlasElement.getElementsByTagName("asset");
					for(int i=0; i<assetNList.getLength();i++){
						Node assetNode = assetNList.item(i);
						if (assetNode.getNodeType() == Node.ELEMENT_NODE) {
							Element assetElement = (Element) assetNode;
							String assetName = assetElement.getAttribute("name");
							
							Asset asset = new Asset(atlas,assetName);
							
							ArrayList<String> animationFrames = new ArrayList<String>();
							NodeList frameNList = assetElement.getElementsByTagName("frame");
							for(int f=0;f<frameNList.getLength();f++){
								Node frameNode = frameNList.item(f);
								if (frameNode.getNodeType() == Node.ELEMENT_NODE) {
									Element frameElement = (Element) frameNode;
									animationFrames.add(frameElement.getAttribute("texture"));
								}
							}
							asset.setFrames(animationFrames);
							atlas.getAssets().add(asset);
						}
					}
					atlasesBase.add(atlas);
				}
			}
			return atlasesBase;
		    } catch (Exception e) {
			e.printStackTrace();
			return null;
		    }
		}
	
	public boolean saveAtlasesBase(){
		try {
			// root elements
			Document doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("atlasesbase");
			doc.appendChild(rootElement);

			for(Atlas atl : GOBase.atlasesBase){;
				Element atlasElement = doc.createElement("atlas");
				rootElement.appendChild(atlasElement);

				atlasElement.setAttribute("name", atl.getName());
				
				for(Asset ast : atl.getAssets()){
					Element assetElement = doc.createElement("asset");
					assetElement.setAttribute("name", ast.getAssetName());
					for(int i=0;i<ast.getFrames().size();i++){
						Element frameElement = doc.createElement("frame");
						frameElement.setAttribute("texture", ast.getFrameName(i));
						assetElement.appendChild(frameElement);
					}
					atlasElement.appendChild(assetElement);
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(ATLAS_BASE_ADDRESS));
			
			transformer.transform(source, result);
			
			return true;
		} 
		catch (Exception ex) {
			  ex.printStackTrace();
			  
			  return false;
		}
	}
	
}
