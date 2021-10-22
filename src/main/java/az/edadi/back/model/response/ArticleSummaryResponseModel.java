package az.edadi.back.model.response;

import az.edadi.back.constants.PhotoEnum;
import az.edadi.back.entity.Article;
import az.edadi.back.model.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.Date;
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSummaryResponseModel {
    private  String title;
    private  String slug;
    private  String description;
    private  String imageUrl;
    private UserSummary author;
    private Date birthDay;

    public ArticleSummaryResponseModel(Article article){
        title=article.getTitle();
        slug=article.getSlug();
        description=article.getDescription();
        birthDay =article.getDate();
        author=new UserSummary(article.getUser());
        imageUrl= PhotoEnum.ROOT_PHOTO_URL.getName()+PhotoEnum.BLOG_IMAGE_FOLDER.getName()+"/"+article.getCoverUrl();
    }
}
