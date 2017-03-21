import org.xmldb.api.base.XMLDBException;

/**
 * Created by aacerete on 20/03/17.
 */

import net.xqj.exist.ExistXQDataSource;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.modules.CollectionManagementService;
import javax.xml.xquery.*;
import java.io.File;

public class ExistDBJava {

    private static final String usuari = "admin";
    private static final String contrasenya = "root";
    private static final String path = "mondial.xml";
    private static final String novaColeccio ="aacereteCollection";
    private static final String PORT = "8080";
    private static final String IP = "localhost";
    private static final String URI = "xmldb:exist://localhost:8080/exist/xmlrpc";
    private static final String driver = "org.exist.xmldb.DatabaseImpl";

    public static void main(String args[]){

        try{

            //Creem una col·leccio
            crearColeccio();
            System.out.println("Col·lecció creada");

            //Afegim un arxiu Xml a la col·lecció
            afegirRecursColeccio();
            System.out.println("Arxiu xml afegit");


            //Fem una query sobre els arxius de la nostra col·leccio i separem els resultats a partir de splits
            String[] resultats = realitzarQuery("collection('aacereteDB')/mondial/country").replaceAll("</country>","").split("<country>");

            for (String resultat: resultats) {

                System.out.print(resultat);

            }

        }catch (XMLDBException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            System.out.println(e);
        }catch (IllegalAccessException e){
            System.out.println(e);
        }catch (InstantiationException e){
            System.out.println(e);
        }catch (XQException e){
            System.out.println(e);
        }
    }

    private static void crearColeccio() throws XMLDBException, ClassNotFoundException, IllegalAccessException, InstantiationException{

        //Realitzem la connexio
        Class clas = Class.forName(driver);
        Database database = (Database) clas.newInstance();
        database.setProperty("create-database", "true");
        DatabaseManager.registerDatabase(database);

        //Creem la nova col·lecció
        Collection parent = DatabaseManager.getCollection(URI+"/db", usuari, contrasenya);
        CollectionManagementService c = (CollectionManagementService) parent.getService("CollectionManagementService", "1.0");
        c.createCollection("aacereteCollection");
    }

    private static void afegirRecursColeccio() throws XMLDBException, ClassNotFoundException, IllegalAccessException, InstantiationException{

        File file = new File(path);

        //Realitzem la connexio
        Class classe = Class.forName(driver);
        Database database = (Database) classe.newInstance();
        database.setProperty("create-database", "true");

       //Creem manegador
        DatabaseManager.registerDatabase(database);

        //Instanciem la col·lecció a la que afegim el recurs xml
        Collection collection = DatabaseManager.getCollection(URI+"/db/"+ novaColeccio, usuari, contrasenya);

        //Afegim el recurs
        Resource resource = collection.createResource(path, "XMLResource");
        resource.setContent(file);
        collection.storeResource(resource);
    }

    private static String realitzarQuery(String query) throws XQException{

        //Realitzem la connexio
        XQDataSource source = new ExistXQDataSource();
        source.setProperty("serverName", IP);
        source.setProperty("port", PORT);
        XQConnection connection = source.getConnection();

        //Preparem i executem la consulta
        XQPreparedExpression expression = connection.prepareExpression(query);
        XQResultSequence result = expression.executeQuery();

        //Ordenem el resultat de la consulta
        String resultado="";
        String linea;

        while (result.next()){

            linea = result.getItemAsString(null);
            resultado = resultado+"\n"+linea;
        }

        connection.close();

        return resultado;
    }
}