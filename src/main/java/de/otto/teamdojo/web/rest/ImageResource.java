package de.otto.teamdojo.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.otto.teamdojo.service.ImageQueryService;
import de.otto.teamdojo.service.ImageService;
import de.otto.teamdojo.service.dto.ImageCriteria;
import de.otto.teamdojo.service.dto.ImageDTO;
import de.otto.teamdojo.web.rest.errors.BadRequestAlertException;
import de.otto.teamdojo.web.rest.util.HeaderUtil;
import de.otto.teamdojo.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * REST controller for managing Image.
 */
@RestController
@RequestMapping("/api")
public class ImageResource {

    private final Logger log = LoggerFactory.getLogger(ImageResource.class);

    private static final String ENTITY_NAME = "image";

    private final ImageService imageService;

    private final ImageQueryService imageQueryService;

    public ImageResource(ImageService imageService, ImageQueryService imageQueryService) {
        this.imageService = imageService;
        this.imageQueryService = imageQueryService;
    }

    /**
     * POST  /images : Create a new image.
     *
     * @param imageDTO the imageDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new imageDTO, or with status 400 (Bad Request) if the image has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/images")
    @Timed
    public ResponseEntity<ImageDTO> createImage(@Valid @RequestBody ImageDTO imageDTO) throws URISyntaxException {
        log.debug("REST request to save Image : {}", imageDTO);
        if (imageDTO.getId() != null) {
            throw new BadRequestAlertException("A new image cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ImageDTO result = imageService.save(imageDTO);
        return ResponseEntity.created(new URI("/api/images/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /images : Updates an existing image.
     *
     * @param imageDTO the imageDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated imageDTO,
     * or with status 400 (Bad Request) if the imageDTO is not valid,
     * or with status 500 (Internal Server Error) if the imageDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/images")
    @Timed
    public ResponseEntity<ImageDTO> updateImage(@Valid @RequestBody ImageDTO imageDTO) throws URISyntaxException {
        log.debug("REST request to update Image : {}", imageDTO);
        if (imageDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ImageDTO result = imageService.save(imageDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, imageDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /images : get all the images.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of images in body
     */
    @GetMapping("/images")
    @Timed
    public ResponseEntity<List<ImageDTO>> getAllImages(ImageCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Images by criteria: {}", criteria);
        Page<ImageDTO> page = imageQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/images");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
    * GET  /images/count : count all the images.
    *
    * @param criteria the criterias which the requested entities should match
    * @return the ResponseEntity with status 200 (OK) and the count in body
    */
    @GetMapping("/images/count")
    @Timed
    public ResponseEntity<Long> countImages(ImageCriteria criteria) {
        log.debug("REST request to count Images by criteria: {}", criteria);
        return ResponseEntity.ok().body(imageQueryService.countByCriteria(criteria));
    }

    /**
     * GET  /images/:id : get the "id" image.
     *
     * @param id the id of the imageDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the imageDTO, or with status 404 (Not Found)
     */
    @GetMapping("/images/{id}")
    @Timed
    public ResponseEntity<ImageDTO> getImage(@PathVariable Long id) {
        log.debug("REST request to get Image : {}", id);
        Optional<ImageDTO> imageDTO = imageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(imageDTO);
    }

    /**
     * GET  /images/:id : get the "id" image.
     *
     * @param id the id of the imageDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the imageDTO, or with status 404 (Not Found)
     */
    @GetMapping("/images/{id}/content")
    @Timed
    public ResponseEntity<byte[]> getImageContent(@PathVariable Long id, @RequestParam(value="size", required=false) String size) {
        log.debug("REST request to get Image : {}", id);
        Optional<ImageDTO> imageDTO = imageService.findOne(id);
        if (!imageDTO.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ImageDTO image = imageDTO.get();

        size = size == null ? "LARGE" : size.toUpperCase();
        byte[] imageBlob;
        String contentType;
        if (size.equals("SMALL")) {
            imageBlob = image.getSmall();
            contentType = image.getSmallContentType();
        } else if (size.equals("MEDIUM")) {
            imageBlob = image.getMedium();
            contentType = image.getMediumContentType();
        } else {
            imageBlob = image.getLarge();
            contentType = image.getLargeContentType();
        }
        return ResponseEntity.ok()
            .header("Content-Type", contentType)
            .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
            .body(imageBlob);
    }

    /**
     * GET  /images/name/:name : get the "name" image.
     *
     * @param name the name of the imageDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the imageDTO, or with status 404 (Not Found)
     */
    @GetMapping("/images/name/{name}")
    @Timed
    public ResponseEntity<byte[]> getImageContent(@PathVariable String name) {
        log.debug("REST request to get Image : {}", name);
        Optional<ImageDTO> imageDTO = imageService.findByName(name);
        if (!imageDTO.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ImageDTO image = imageDTO.get();

        byte[] imageBlob;
        String contentType;
            imageBlob = image.getLarge();
            contentType = image.getLargeContentType();
        return ResponseEntity.ok()
            .header("Content-Type", contentType)
            .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
            .body(imageBlob);
    }

    /**
     * DELETE  /images/:id : delete the "id" image.
     *
     * @param id the id of the imageDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/images/{id}")
    @Timed
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        log.debug("REST request to delete Image : {}", id);
        imageService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
