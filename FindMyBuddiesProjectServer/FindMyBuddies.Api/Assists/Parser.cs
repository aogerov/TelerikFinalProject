using System;
using System.Collections.Generic;
using System.Linq;
using FindMyBuddies.Api.Models;
using FindMyBuddies.Model;

namespace FindMyBuddies.Api.Assists
{
    public class Parser
    {
        public static User UserModelToUser(UserModel userModel)
        {
            return new User
            {
                Username = userModel.Username.Trim().ToLower(),
                Nickname = userModel.Nickname.Trim(),
                AuthCode = userModel.AuthCode.Trim()
            };
        }

        public static UserLoggedModel UserToUserLoggedModel(User user)
        {
            return new UserLoggedModel
            {
                Nickname = user.Nickname,
                SessionKey = user.SessionKey
            };
        }

        public static Coordinates CreateDefaultCoordinates(UserModel model)
        {
            return new Coordinates
            {
                Latitude = "0",
                Longitude = "0"
            };
        }

        public static Coordinates CoordinatesModelToCoordinates(CoordinatesModel coordinatesModel)
        {
            return new Coordinates
            {
                Latitude = coordinatesModel.Latitude,
                Longitude = coordinatesModel.Longitude
            };
        }

        public static FriendModel FriendToFriendModel(User user)
        {
            var friendModel = new FriendModel
                 {
                     Id = user.Id,
                     Nickname = user.Nickname,
                     IsOnline = user.IsOnline,
                     Latitude = user.Coordinates.Latitude,
                     Longitude = user.Coordinates.Longitude,
                     CoordinatesTimestamp = user.Coordinates.Timestamp
                 };

                return friendModel;
        }

        public static FriendModel UserToFriendFoundModel(User friendFound)
        {
            if (friendFound == null)
            {
                return null;
            }

            return new FriendModel
            {
                Id = friendFound.Id,
                Nickname = friendFound.Nickname,
                IsOnline = friendFound.IsOnline
            };
        }

        public static FriendRequest CreateFriendRequest(User userThatMakesRequest)
        {
            return new FriendRequest
            {
                FromUserId = userThatMakesRequest.Id,
                FromUserNickname = userThatMakesRequest.Nickname
            };
        }

        public static IEnumerable<FriendRequestModel> FriendRequestsToFriendRequestModels(
            IOrderedEnumerable<FriendRequest> friendRequestsEntities)
        {
            var friendRequestModels =
                (from friendRequestEntity in friendRequestsEntities
                 select new FriendRequestModel
                 {
                     FromUserId = friendRequestEntity.FromUserId,
                     FromUserNickname = friendRequestEntity.FromUserNickname,
                     IsShowed = friendRequestEntity.IsShowed
                 });

            return friendRequestModels;
        }

        public static ImageModel ImageToImageModel(Image image)
        {
            return new ImageModel
            {
                Url = image.Url,
                Timestamp = image.Timestamp,
                LatitudeAtCapturing = image.Coordinates.Latitude,
                LongitudeAtCapturing = image.Coordinates.Longitude
            };
        }

        public static Image ImageModelToImage(ImageModel imageModel)
        {
            return new Image
            {
                Url = imageModel.Url,
                Timestamp = imageModel.Timestamp,
                Coordinates = new Coordinates
                {
                    Latitude = imageModel.LatitudeAtCapturing,
                    Longitude = imageModel.LongitudeAtCapturing
                }
            };
        }
    }
}