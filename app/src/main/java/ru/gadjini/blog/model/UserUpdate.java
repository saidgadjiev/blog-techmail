package ru.gadjini.blog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Email;
import java.util.Objects;

/**
 * Информация о пользователе. 
 */
@ApiModel(description = "Информация о пользователе. ")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-09-04T11:20:41.242340400+03:00[Europe/Moscow]")

public class UserUpdate {
  @JsonProperty("fullname")
  private String fullname;

  @JsonIgnore
  private boolean fullnameSet;

  @JsonProperty("about")
  private String about;

  @JsonIgnore
  private boolean aboutSet;

  @JsonProperty("email")
  private String email;


  @JsonIgnore
  private boolean emailSet;

  public UserUpdate fullname(String fullname) {
    this.fullname = fullname;
    this.fullnameSet = true;
    return this;
  }

  /**
   * Полное имя пользователя.
   * @return fullname
  */
  @ApiModelProperty(example = "Captain Jack Sparrow", value = "Полное имя пользователя.")


  public String getFullname() {
    return fullname;
  }

  public void setFullname(String fullname) {
    this.fullname = fullname;
    this.fullnameSet = true;
  }

  public UserUpdate about(String about) {
    this.about = about;
    this.aboutSet = true;
    return this;
  }

  /**
   * Описание пользователя.
   * @return about
  */
  @ApiModelProperty(example = "This is the day you will always remember as the day that you almost caught Captain Jack Sparrow!", value = "Описание пользователя.")


  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
    this.aboutSet = true;
  }

  public UserUpdate email(String email) {
    this.email = email;
    this.emailSet = true;
    return this;
  }

  /**
   * Почтовый адрес пользователя (уникальное поле).
   * @return email
  */
  @ApiModelProperty(example = "captaina@blackpearl.sea", value = "Почтовый адрес пользователя (уникальное поле).")

@Email
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
    this.emailSet = true;
  }

  public boolean isEmailSet() {
    return emailSet;
  }

  public boolean isAboutSet() {
    return aboutSet;
  }

  public boolean isFullnameSet() {
    return fullnameSet;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserUpdate userUpdate = (UserUpdate) o;
    return Objects.equals(this.fullname, userUpdate.fullname) &&
        Objects.equals(this.about, userUpdate.about) &&
        Objects.equals(this.email, userUpdate.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fullname, about, email);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserUpdate {\n");
    
    sb.append("    fullname: ").append(toIndentedString(fullname)).append("\n");
    sb.append("    about: ").append(toIndentedString(about)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

