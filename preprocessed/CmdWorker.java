import java.util.PriorityQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.File;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;






final class Key {
    public Function<String, Void> func;
    public String description;

    public Key(Function<String, Void> func, String description) {
        this.func = func;
        this.description = description;
    }
}

public class CmdWorker {
    protected Map<String, Key> cmdMap;
    protected PriorityQueue<City> priorityQueue;
    protected Date initDate;
    protected Date lastChangeDate;
    protected final String CMD_NOT_FOUND = "command not found";
    protected final String VOID_STR = "void";

    {
        initDate = new Date();
        lastChangeDate = initDate;
        cmdMap = new HashMap<>();

        

        cmdMap.put("add", new Key(this:: add,  "добавить новый элемент в коллекцию"));;
        cmdMap.put("remove_first", new Key(this:: removeFirst,  "удалить первый элемент из коллекции"));;
        cmdMap.put("show", new Key(this:: show,  "вывести в стандартный поток вывода все элементы коллекции в строковом представлении"));;
        cmdMap.put("info", new Key(this:: info,  "вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)"));;
        cmdMap.put("remove_lower", new Key(this:: removeLower,  "удалить из коллекции все элементы, меньшие, чем заданный"));;
        cmdMap.put("remove", new Key(this:: remove,  "удалить элемент из коллекции по его значению"));;
        cmdMap.put("add_if_max", new Key(this:: addIfMax,  "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции"));;
        cmdMap.put("list", new Key(this:: list,  "просмотр списка команд"));;
        cmdMap.put("help", new Key(this:: list,  "просмотр списка команд"));;
        cmdMap.put("man", new Key(this:: man,  "описание команд"));;
        cmdMap.put("exit", new Key(this:: exit,  "выход"));;

        
    }

    public CmdWorker() {
        priorityQueue = new PriorityQueue<>();
    }

    public void readFromFile(final String filename) {
        try {
            FileReader reader = new FileReader(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(reader));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("city");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element)nNode;
                    priorityQueue.add(new City(eElement.getElementsByTagName("name").item(0).getTextContent(), Integer.parseInt(eElement.getElementsByTagName("size").item(0).getTextContent())));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printToFile(final String filename) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element cityCont = document.createElement("city-cont");
            document.appendChild(cityCont);

            Element city, name, size;
            for (City elem : priorityQueue) {
                city = document.createElement("city");
                cityCont.appendChild(city);

                name = document.createElement("name");
                name.appendChild(document.createTextNode(elem.name));
                city.appendChild(name);

                size = document.createElement("size");
                size.appendChild(document.createTextNode(elem.areaSize.toString()));
                city.appendChild(size);
            }

            DOMSource domSource = new DOMSource(document);
            StreamResult result = new StreamResult(new BufferedWriter(new FileWriter(filename)));

            // create the xml file
            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected City processInput(String jsonInput) {
        City city = null;
        String name;
        Integer size;

        // System.out.println(jsonInput.indexOf('}'));
        // System.out.println(jsonInput.length());
        if (jsonInput.indexOf('}') < jsonInput.length() - 1) {
            System.out.println("uncorrect command");
            return city;
        }

        try {
            JSONObject obj = new JSONObject(jsonInput);

            try {
                name = obj.getString("name");
            } catch (JSONException e) {
                name = "Unnamed";
            }

            try {
                size = obj.getInt("size");
            } catch (JSONException e) {
                size = 0;
            }

            city = new City(name, size);

        } catch (JSONException e) {
            System.out.println("Введите элемент!");
            return city;
        }

        return city;
    }

    public void doCmd(String cmd) {
        int indexOfSpace = cmd.indexOf(' ');
        indexOfSpace = indexOfSpace == -1 ? cmd.length() : indexOfSpace;
        String[] splittedCmd = {cmd.substring(0, indexOfSpace), indexOfSpace != cmd.length() ? cmd.substring(indexOfSpace + 1, cmd.length()).trim() : VOID_STR};
        try {
            cmdMap.get(splittedCmd[0]).func.apply(splittedCmd[1]);
        } catch (NullPointerException e) {
            System.out.println(CMD_NOT_FOUND);
        } catch(Exception e) {
            System.out.println("uncorrect command syntax");
        }
    }

    

    

    

    





    public Void removeLower(String jsonElem) {
        City city = processInput(jsonElem);
        if (city == null) return null;;

        for (Iterator<City> iterator = priorityQueue.iterator(); iterator.hasNext(); ) {
            City elem = iterator.next();
            if (elem.compareTo(city) < 0) {
                iterator.remove();
                lastChangeDate = new Date();
                return null;
            }
        }

        return null;
    }

    





    public Void addIfMax(String jsonElem) {
        City city = processInput(jsonElem);
        if (city == null) return null;;

        for (City elem : priorityQueue)
            if (elem.compareTo(city) >= 0)
                return null;

        priorityQueue.add(city);
        lastChangeDate = new Date();
        return null;
    }

    





    public Void remove(String jsonElem) {
        City city = processInput(jsonElem);
        if (city == null) return null;;

        for (Iterator<City> iterator = priorityQueue.iterator(); iterator.hasNext(); ) {
            City elem = iterator.next();
            if (elem.equals(city)) {
                iterator.remove();
                lastChangeDate = new Date();
                return null;
            }
        }

        return null;
    }

    





    public Void add(String jsonElem) {
        City city = processInput(jsonElem);
        if (city == null) return null;;

        priorityQueue.add(city);
        lastChangeDate = new Date();
        return null;
    }

    

    





    public Void info(String NULL) {
        System.out.println("Класс коллекции: " + priorityQueue.getClass().getName());
        try {
            System.out.println("Класс элементов: " + priorityQueue.peek().getClass().getName());
        } catch(Exception e) {}
        System.out.println("Колиечество элементов: " + priorityQueue.size());
        System.out.printf("Дата инициализации: %s\n",  initDate.toLocaleString());
        System.out.printf("Дата последнего изменения: %s\n",  lastChangeDate.toLocaleString());
        return null;
    }

    





    public Void show(String NULL) {
        for (City elem : priorityQueue)
            System.out.printf("%s\n",  elem);
        return null;
    }

    





    public Void removeFirst(String NULL) {
        priorityQueue.poll();
        lastChangeDate = new Date();
        return null;
    }

    





    public Void exit(String NULL) {
        Main.release();
        return null;
    }

    





    public Void man(String cmd) {
        try {
            System.out.println(cmdMap.get(cmd).description);
        } catch (NullPointerException e) {
            System.out.println("right syntax is man <command>");
        }
        return null;
    }

    





    public Void list(String NULL) {
        for (String cmd : cmdMap.keySet())
            System.out.println(cmd);

        return null;
    }

    

    

    public void sort() {
        Arrays.sort(priorityQueue.toArray());
    }
}

