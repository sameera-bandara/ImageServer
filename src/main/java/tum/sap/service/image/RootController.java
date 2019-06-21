package tum.sap.service.image;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class RootController {

    private static final Logger log = LoggerFactory.getLogger(RootController.class);

    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getImage(@RequestParam String image_name) {

        DB db = mongoTemplate.getDb();
        DBCollection collection = mongoTemplate.getCollection("images");

        GridFS gfsPhoto = new GridFS(db, "production_images");
        GridFSDBFile imageForOutput = gfsPhoto.findOne(image_name);

        if (imageForOutput == null)
            return new ResponseEntity<Object>(null, HttpStatus.NOT_FOUND);
        else {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + image_name);
            InputStreamResource resource = new InputStreamResource(imageForOutput.getInputStream());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(imageForOutput.getLength())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        }

    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> saveImage(@RequestParam("file") MultipartFile file) {

        DB db = mongoTemplate.getDb();
        DBCollection collection = mongoTemplate.getCollection("images");

        GridFS gfsPhoto = new GridFS(db, "production_images");
        GridFSInputFile gfsFile = null;
        try {
            gfsFile = gfsPhoto.createFile(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<Object>("AN error occurred while saving image", HttpStatus.NO_CONTENT);
        }
        gfsFile.setFilename(file.getOriginalFilename());
        gfsFile.save();

        return new ResponseEntity<Object>("Image added successfully", HttpStatus.CREATED);
    }
}
