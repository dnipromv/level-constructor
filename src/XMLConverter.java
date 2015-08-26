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
	
	//Returns list of prefabs currently existing in prafabsbase.xml file
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
					String textureAddress= eElement.getElementsByTagName("texture").item(0).getTextContent();
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
					
					prefabs.add(new Prefab(ID,categoryID,tiledWidth,tiledHeight,textureAddress,description,additiveAttributes));
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
				
				Element texture = doc.createElement("texture");
				texture.appendChild(doc.createTextNode(P.getTextureAddress()));
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
			
			Element objectsElement = doc.createElement("objects");
			rootElement.appendChild(objectsElement);
			
			for(GameObject go : ConstructorWindow.globals.level.getObjects()){
				Element gameObject = doc.createElement("gameobject");
				objectsElement.appendChild(gameObject);
				
				gameObject.setAttribute("prefab", go.getPrefab().getPrefabID());
				gameObject.setAttribute("posx", String.valueOf(go.getPosition().x));
				gameObject.setAttribute("posy", String.valueOf(go.getPosition().y));
				gameObject.setAttribute("width", String.valueOf(go.getTiledWidth()));
				gameObject.setAttribute("height", String.valueOf(go.getTiledHeight()));
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileAddress));

			transformer.transform(source, result);

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
			
			Element objectsElement = (Element)levelElement.getElementsByTagName("objects").item(0);
			NodeList nList = objectsElement.getElementsByTagName("gameobject");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					System.out.println(GOBase.prefabsBase.get(eElement.getAttribute("prefab")).getTiledWidth());
					//System.out.println(eElement.getAttribute("prefab"));
					Prefab prefab = GOBase.prefabsBase.get(eElement.getAttribute("prefab"));
					int posX = Integer.valueOf(eElement.getAttribute("posx"));
					int posY = Integer.valueOf(eElement.getAttribute("posy"));
					
					gameObjects.add(new GameObject(prefab,posX,posY));
				}
			}
			level.setObjects(gameObjects);
			return level;
		    } catch (Exception e) {
			e.printStackTrace();
			return null;
		    }
		
	}
}
