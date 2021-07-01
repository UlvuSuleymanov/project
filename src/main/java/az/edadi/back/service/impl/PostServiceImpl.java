package az.edadi.back.service.impl;

import az.edadi.back.entity.User;
import az.edadi.back.entity.post.Post;
import az.edadi.back.entity.post.PostVote;
import az.edadi.back.repository.*;
import az.edadi.back.service.FileService;
import az.edadi.back.service.ImageService;
import az.edadi.back.service.S3Service;
import az.edadi.back.entity.university.Speciality;
import az.edadi.back.entity.university.University;
import az.edadi.back.model.response.PostResponseModel;
 import az.edadi.back.utility.AuthUtil;
import az.edadi.back.utility.ImageUtil;
 import az.edadi.back.model.request.PostRequestModel;
import az.edadi.back.model.response.SearchResultResponseModel;
import az.edadi.back.service.PostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.util.*;


@Service
public class PostServiceImpl implements PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TagServiceImpl tagService;
    private final UniversityRepository universityRepository;
    private final FileService s3Service;
    private final ImageService imageService;
    private final PostVoteRepository postVoteRepository;
    private final SpecialityRepository specialityRepository;

    @Autowired
    public PostServiceImpl(UserRepository userRepository,
                           PostRepository postRepository,
                           TagServiceImpl tagService,
                           UniversityRepository universityRepository,
                           FileService s3Service,
                           ImageService imageService,
                           PostVoteRepository postVoteRepository,
                           SpecialityRepository specialityRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.universityRepository = universityRepository;
        this.s3Service = s3Service;
        this.imageService = imageService;
        this.postVoteRepository = postVoteRepository;
        this.specialityRepository = specialityRepository;
    }


    @Override
    public Post createPost(PostRequestModel postRequestModel, String username) {


        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username or email : ")
        );
        Date date = new Date();

        Post post = new Post();
        post.setUser(user);
        post.setDate(date);
        post.setPostText(postRequestModel.getText());

        switch (postRequestModel.getType()){
            case "university":
                University university = universityRepository.findById(postRequestModel.getId()).orElseThrow(() ->
                        new UsernameNotFoundException("University not found with  id : ")
                );
                post.setUniversity(university);

                break;

            case "speciality":
                Speciality speciality = specialityRepository.findById(postRequestModel.getId()).orElseThrow(() ->
                        new EntityNotFoundException("Speciality not found with  id : ")
                );
                post.setSpeciality(speciality);
                break;

            case "uni-speciality":
                break;
        }





      //  post.setUniversity(universityRepository.getOne(1L));
      //  post.setPostText(postRequestModel.getText());
      //  post.setPostTitle("title");
      //  user.getPosts().add(post);
     //   Post savedPost = postRepository.save(post);

      //  Set<Tag> tagSet = tagService.addTags(postRequestModel.getTags(), savedPost);
    //    post.setTags(tagSet);
      //  String photoUrl = ImageEnum.DEFAULT_IMAGE_NAME.getName();
//        if (postRequestModel.getMultipartFile() != null) {
//            photoUrl = savePostPicture(savedPost.getId(), postRequestModel.getMultipartFile());
//        }
    //    post.setPhotoUrl(photoUrl);
     return     postRepository.save(post);


    }

    @Override
    public List<PostResponseModel> getPosts(Integer page, Integer size, String sort) {

        Pageable pageable = PageRequest.of(page, size);
        Optional<List<Post>> postList = Optional.empty();


        switch (sort) {
            case "mostCommented":
                postList = Optional.ofNullable(postRepository.getTopCommentPost(pageable));
                break;
            case "mostLiked":
                postList = Optional.ofNullable(postRepository.getTopLikedPost(pageable));
                break;
            case "three":
                System.out.println("mostLiked");
                break;
            default:
                postList = Optional.of(postRepository.findAll(pageable).toList());
        }
        if (postList.isPresent()) {
            return postsToResponseModels(postList.get());
        }

        return null;
    }



    @Override
    public List<PostResponseModel> getSpecialityyPosts(Long code, Integer page, Integer size, String sort) {
        Pageable pageable = PageRequest.of(page, size);
        Optional<List<Post>> postList = Optional.empty();


        switch (sort) {
            case "mostCommented":
                postList = Optional.ofNullable(postRepository.getTopCommentPost(pageable));
                break;
            case "mostLiked":
                postList = Optional.ofNullable(postRepository.getTopLikedPost(pageable));
                break;
            case "three":
                System.out.println("mostLiked");
                break;
            default:
                postList = Optional.of(postRepository.findAll(pageable).toList());
        }
        if (postList.isPresent()) {
            return postsToResponseModels(postList.get());
        }

        return null;
    }




    @Override
    public List<PostResponseModel> getUniversityPosts(String uniAbbr,
                                                      Integer page,
                                                      Integer size,
                                                      String sort) {


        Optional<University> universityOptional = universityRepository.findByAbbr(uniAbbr);
        List<Post> postList = null;
        Pageable pageable = PageRequest.of(page, size);

        if (universityOptional.isPresent()) {

            switch (sort) {

                case "mostCommented":
                    postList = postRepository.getTopCommentUniversityPost(universityOptional.get().getId(), pageable);
                    break;
                case "mostLiked":
                    postList = postRepository.getTopLikedUniversityPost(universityOptional.get().getId(), pageable);
                    break;

                default:
                    postList = postRepository.findAll(pageable).toList();

            }

            Optional<List<Post>> postsOptional = Optional.ofNullable(postList);
            if (postsOptional.isPresent())
                return postsToResponseModels(postList);
        }
        return null;


    }


    @Override
    public PostResponseModel toResponse(Post post) {

   if(post!=null) {

       boolean authenticated = AuthUtil.userIsAuthenticated();
       Long userId = null;


       if (authenticated) {
           userId = AuthUtil.getCurrentUserId();
       }

       boolean isLiked = false;
       if (authenticated) {
           isLiked = checkUserIsLiked(userId, post.getId());

       }

       return new PostResponseModel(post, isLiked);

   }
   return null;
    }


    @Override
    public String savePostPicture(Long id, MultipartFile multipartFile) {
        try {
            File file = imageService.convertMultiPartToFile(multipartFile);
            String name = "postImage" + id;
            s3Service.save(name, file);
            file.delete();
            return ImageUtil.getPhotoUrl(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "default";
    }


    @Override
    public PostVote likePost(long postId, Long userId) {
        Post post = postRepository.getOne(postId);
        User user = userRepository.getOne(userId);

        PostVote postVote = postVoteRepository.getPostVoteByIds(userId, postId);
        if (postVote == null) {
            postVote = new PostVote();
            postVote.setUser(user);
            postVote.setPost(post);
            postVote.setDate(new Date());
        }
        return postVoteRepository.save(postVote);


    }

    @Override
    public void disLikePost(long postId, Long userId) {
        PostVote postVote = postVoteRepository.getPostVoteByIds(userId, postId);
        postVoteRepository.delete(postVote);
    }


    @Override
    public boolean checkUserIsLiked(Long userId, Long postId) {
        return postVoteRepository.getPostVoteByIds(userId, postId) != null;

    }

    @Override
    public List<PostResponseModel> postsToResponseModels(List<Post> posts) {

        List<PostResponseModel> postResponseModelList = new ArrayList<>();

        for (Post post : posts) {

            postResponseModelList.add(toResponse(post));
        }

        return postResponseModelList;


    }

    @Override
    public List<SearchResultResponseModel> searchPostTitle(String postTitle) {
        Pageable pageable = PageRequest.of(0, 10);

        List<SearchResultResponseModel> searchResult = postRepository.getPostLikeTitle(postTitle, pageable);

        return searchResult;
    }

    @Override
    public PostResponseModel getPost(Long postId) {
        Optional<Post> post = postRepository.findById(postId);


        if (post.isPresent()) {
            System.out.println("present");

            return toResponse(post.get());
        }

        return null;
    }
}