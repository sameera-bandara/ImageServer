package tum.sap.service.image;

import java.util.Map;

import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Controller
@RequestMapping("/")
public class RootController {

    private static final Logger log = LoggerFactory.getLogger(RootController.class);

    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Object> getImage(@RequestParam String image_id) {

        DBCollection collection = mongoTemplate.getCollection("image_db");

        BasicDBObject dBObject = new BasicDBObject();
        dBObject.put("image_id", image_id);
        DBObject image = collection.findOne(dBObject);

        if (image == null)
            return new ResponseEntity<Object>(image, HttpStatus.NOT_FOUND);
        else
            return new ResponseEntity<Object>(image, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> saveImage(@RequestBody Map<String, String> imageData) {

        BasicDBObject dBObject = new BasicDBObject();
        dBObject.put("image_id", imageData.get("image_id"));
        dBObject.put("value", imageData.get("value"));

        DBCollection collection = mongoTemplate.getCollection("image_db");
        collection.insert(dBObject);

        return new ResponseEntity<Object>("Image added successfully", HttpStatus.CREATED);
    }
}
