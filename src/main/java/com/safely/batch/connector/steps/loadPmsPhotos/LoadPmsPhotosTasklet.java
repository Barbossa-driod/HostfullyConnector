package com.safely.batch.connector.steps.loadPmsPhotos;

import com.safely.api.domain.Organization;
import com.safely.batch.connector.client.PhotosService;
import com.safely.batch.connector.pms.photo.PmsPhoto;
import com.safely.batch.connector.steps.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadPmsPhotosTasklet implements Tasklet {

  private static final Logger log = LoggerFactory.getLogger(LoadPmsPhotosTasklet.class);

  @Autowired
  private JobContext jobContext;

  @Autowired
  private PhotosService photosService;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
      throws Exception {

    Organization organization = jobContext.getOrganization();
    log.info("Loading photos from PMS");
    Map<Integer, List<PmsPhoto>> propertyImages = new HashMap<>();

//        String key = jobContext.getServerKey();
//        String secret = jobContext.getServerSecret();
//        List<PmsProperty> properties = jobContext.getPmsProperties();
//

//        for (PmsProperty property : properties) {
//            List<PmsPhoto> images = getPropertyImages(key, secret, property);
//
//            log.info("Loaded {} photos from PMS for property: {}.", images.size(), property.getId());
//            propertyImages.put(property.getId(), images);
//        }

    jobContext.setPmsPropertyPhotos(propertyImages);

    return RepeatStatus.FINISHED;
  }

//  private List<PmsPhoto> getPropertyImages(String key, String secret, PmsProperty property)
//      throws Exception {
//    int page = 0;
//    List<PmsPhoto> images = new ArrayList<>();
//    ImagesRoot imagesRoot;
//    do {
//      // increment counter before call, must start at page = 1
//      page++;
//      imagesRoot = imageClient.getImages(property.getId(), page, key, secret);
//
//      images.addAll(imagesRoot.get_embedded().getImages());
//
//      // keep loading images until we are on the last page of results
//    } while (imagesRoot.getPage_count() > page);
//    return images;
//  }
}
