package tum.sap.service.image;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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

    /**
     * Endpoint to retrieve an image for given image name
     *
     * @param key
     * @return the image for specified image_name with HttpStatus.OK. If the image is not found return HttpStatus.NO_CONTENT
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getImage(@RequestParam String key) {

        DB db = mongoTemplate.getDb();

        GridFS gfsPhoto = new GridFS(db, "product_images");
        GridFSDBFile imageForOutput = gfsPhoto.findOne(key);

        if (imageForOutput == null)
            return new ResponseEntity<Object>(null, HttpStatus.NO_CONTENT);
        else {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key);
            InputStreamResource resource = new InputStreamResource(imageForOutput.getInputStream());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(imageForOutput.getLength())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        }
    }

    /**
     * Endpoint to store an images
     *
     * @param file image file
     * @return HttpStatus.CREATED if the image is saved successfully. If any error occured HttpStatus.INTERNAL_SERVER_ERROR is returned
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> saveImage(@RequestParam("image") MultipartFile file) {

        DB db = mongoTemplate.getDb();

        GridFS gfsPhoto = new GridFS(db, "product_images");

        GridFSInputFile gfsFile = null;
        try {
            gfsFile = gfsPhoto.createFile(file.getInputStream());
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<Object>("AN error occurred while saving image", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //generate a unique identifier for image
        UUID key = UUID.randomUUID();
        gfsFile.setFilename(key.toString());
        gfsFile.save();

        return new ResponseEntity<Object>(key.toString(), HttpStatus.CREATED);
    }
}
