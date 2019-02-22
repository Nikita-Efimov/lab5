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

#define println(args) System.out.println(args)
#define printf(format, args) System.out.printf(format, args)
#define COLLECTION_TYPE PriorityQueue
#define PRIOR_TYPE City

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
    protected COLLECTION_TYPE<PRIOR_TYPE> priorityQueue;
    protected Date initDate;
    protected Date lastChangeDate;
    protected final String CMD_NOT_FOUND = "command not found";
    protected final String VOID_STR = "void";

    {
        initDate = new Date();
        lastChangeDate = initDate;
        cmdMap = new HashMap<>();

        #define addCmd(name, func, desc) cmdMap.put(name, new Key(this::func, desc));

        addCmd("add", add, "добавить новый элемент в коллекцию");
        addCmd("remove_first", removeFirst, "удалить первый элемент из коллекции");
        addCmd("show", show, "вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        addCmd("info", info, "вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)");
        addCmd("remove_lower", removeLower, "удалить из коллекции все элементы, меньшие, чем заданный");
        addCmd("remove", remove, "удалить элемент из коллекции по его значению");
        addCmd("add_if_max", addIfMax, "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции");
        addCmd("list", list, "просмотр списка команд");
        addCmd("help", list, "просмотр списка команд");
        addCmd("man", man, "описание команд");
        addCmd("exit", exit, "выход");

        #undef addCmd
    }

    public CmdWorker() {
        priorityQueue = new COLLECTION_TYPE<>();
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
            for (PRIOR_TYPE elem : priorityQueue) {
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

    protected PRIOR_TYPE processInput(String jsonInput) {
        PRIOR_TYPE city = null;
        String name;
        Integer size;

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

            city = new PRIOR_TYPE(name, size);

        } catch (JSONException e) {
            println("Введите элемент!");
            return city;
        }

        return city;
    }

    public void doCmd(String cmd) {
        int indexOfSpace = cmd.indexOf(' ');
        indexOfSpace = indexOfSpace == -1 ? cmd.length() : indexOfSpace;
        String[] splittedCmd = {cmd.substring(0, indexOfSpace), indexOfSpace != cmd.length() ? cmd.substring(indexOfSpace + 1, cmd.length()) : VOID_STR};
        try {
            cmdMap.get(splittedCmd[0]).func.apply(splittedCmd[1]);
        } catch (NullPointerException e) {
            println(CMD_NOT_FOUND);
        }
    }

    #define void Void

    #define return return null

    #define check() if (city == null) return;

    /**
     * <p>Удаляет из коллекции все элементы, меньшие, чем заданный.</p>
     *
     * @param jsonElem Строка где содержится json элемент
     * @return null, возвращает null в любом случае
     */
    public void removeLower(String jsonElem) {
        PRIOR_TYPE city = processInput(jsonElem);
        check();

        for (Iterator<PRIOR_TYPE> iterator = priorityQueue.iterator(); iterator.hasNext(); ) {
            PRIOR_TYPE elem = iterator.next();
            if (elem.compareTo(city) < 0) {
                iterator.remove();
                lastChangeDate = new Date();
                return;
            }
        }

        return;
    }

    /**
     * <p>Добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции.</p>
     *
     * @param jsonElem Строка где содержится json элемент
     * @return null, возвращает null в любом случае
     */
    public void addIfMax(String jsonElem) {
        PRIOR_TYPE city = processInput(jsonElem);
        check();

        for (PRIOR_TYPE elem : priorityQueue)
            if (elem.compareTo(city) >= 0)
                return;

        priorityQueue.add(city);
        lastChangeDate = new Date();
        return;
    }

    /**
     * <p>Удалить элемент из коллекции по его значению.</p>
     *
     * @param jsonElem Строка где содержится json элемент
     * @return null, возвращает null в любом случае
     */
    public void remove(String jsonElem) {
        PRIOR_TYPE city = processInput(jsonElem);
        check();

        for (Iterator<PRIOR_TYPE> iterator = priorityQueue.iterator(); iterator.hasNext(); ) {
            PRIOR_TYPE elem = iterator.next();
            if (elem.equals(city)) {
                iterator.remove();
                lastChangeDate = new Date();
                return;
            }
        }

        return;
    }

    /**
     * <p>Добавить новый элемент в коллекцию.</p>
     *
     * @param jsonElem Строка где содержится json элемент
     * @return null, возвращает null в любом случае
     */
    public void add(String jsonElem) {
        PRIOR_TYPE city = processInput(jsonElem);
        check();

        priorityQueue.add(city);
        lastChangeDate = new Date();
        return;
    }

    #undef check

    /**
     * <p>Вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.).</p>
     *
     * @param NULL строка где будет пустая строка или VOID_STR
     * @return null, возвращает null в любом случае
     */
    public void info(String NULL) {
        println("Класс коллекции: " + priorityQueue.getClass().getName());
        try {
            println("Класс элементов: " + priorityQueue.peek().getClass().getName());
        } catch(Exception e) {}
        println("Колиечество элементов: " + priorityQueue.size());
        printf("Дата инициализации: %s\n", initDate.toLocaleString());
        printf("Дата последнего изменения: %s\n", lastChangeDate.toLocaleString());
        return;
    }

    /**
     * <p>Вывести в стандартный поток вывода все элементы коллекции в строковом представлении.</p>
     *
     * @param NULL строка где будет пустая строка или VOID_STR
     * @return null, возвращает null в любом случае
     */
    public void show(String NULL) {
        for (PRIOR_TYPE elem : priorityQueue)
            printf("%s\n", elem);
        return;
    }

    /**
     * <p>Удалить первый элемент из коллекции.</p>
     *
     * @param NULL строка где будет пустая строка или VOID_STR
     * @return null, возвращает null в любом случае
     */
    public void removeFirst(String NULL) {
        priorityQueue.poll();
        lastChangeDate = new Date();
        return;
    }

    /**
     * <p>Выход.</p>
     *
     * @param NULL строка где будет пустая строка или VOID_STR
     * @return null, возвращает null в любом случае
     */
    public void exit(String NULL) {
        Main.release();
        return;
    }

    /**
     * <p>Команда для просмотра описания команд.</p>
     *
     * @param cmd Строка где содержится команда для просмотра ее описания
     * @return null, возвращает null в любом случае
     */
    public void man(String cmd) {
        try {
            println(cmdMap.get(cmd).description);
        } catch (NullPointerException e) {
            println("right syntax is man <command>");
        }
        return;
    }

    /**
     * <p>Просмотр списка команд.</p>
     *
     * @param NULL строка где будет пустая строка или VOID_STR
     * @return null, возвращает null в любом случае
     */
    public void list(String NULL) {
        for (String cmd : cmdMap.keySet())
            println(cmd);

        return;
    }

    #undef void

    #undef return

    public void sort() {
        Arrays.sort(priorityQueue.toArray());
    }
}
