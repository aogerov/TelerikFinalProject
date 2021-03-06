﻿using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using FindMyBuddies.Api.Assists;
using FindMyBuddies.Api.Models;
using FindMyBuddies.Data;
using FindMyBuddies.Model;

namespace FindMyBuddies.Api.Controllers
{
    public class ImagesController : BaseApiController
    {
        // api/images/set?sessionKey={sessionKey}
        [HttpPost]
        [ActionName("set")]
        public HttpResponseMessage PostSetNewImage(
            [FromBody]ImageModel imageModel, [FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    if (imageModel == null)
                    {
                        return this.Request.CreateResponse(HttpStatusCode.NotFound);
                    }

                    var user = Validator.ValidateSessionKey(context, sessionKey);

                    var coordinates = Parser.ExtractCoordinatesFromImageModel(imageModel);
                    context.Coordinates.Add(coordinates);

                    var image = Parser.ImageModelToImage(imageModel);
                    image.Coordinates = coordinates;
                    context.Images.Add(image);

                    user.Images.Add(image);
                    context.SaveChanges();

                    var response = this.Request.CreateResponse(HttpStatusCode.OK, imageModel);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/images/get?imagesCount={imagesCount}&sessionKey={sessionKey}
        [HttpPost]
        [ActionName("get")]
        public HttpResponseMessage PostGetFriendsImage(
            [FromBody]FriendModel friendModel, [FromUri]int imagesCount, [FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateSessionKey(context, sessionKey);
                    var friend = Validator.ValidateFriendInDb(context, friendModel.Id, friendModel.Nickname);

                    if (!user.Friends.Contains(friend))
                    {
                        return this.Request.CreateResponse(HttpStatusCode.NotFound);
                    }

                    var imageModels = new List<ImageModel>();
                    if (friend.Images.Count > 0 && imagesCount > 0)
                    {
                        var images = new List<Image>(friend.Images);
                        for (int i = friend.Images.Count - 1; i >= 0 && imagesCount > 0; i--, imagesCount--)
                        {
                            var imageModel = Parser.ImageToImageModel(images[i]);
                            imageModels.Add(imageModel);
                        }
                    }

                    var response = this.Request.CreateResponse(HttpStatusCode.OK, imageModels);
                    return response;
                }
            });

            return responseMsg;
        }
    }
}
