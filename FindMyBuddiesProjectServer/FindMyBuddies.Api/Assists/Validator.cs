using System;
using System.Linq;
using FindMyBuddies.Data;
using FindMyBuddies.Model;
using FindMyBuddies.Api.Models;

namespace FindMyBuddies.Api.Assists
{
    public class Validator
    {
        private const int MinUsernameAndNicknameLength = 3;
        private const int MaxUsernameAndNicknameLength = 30;
        private const int AuthcodeLength = 40;
        private const string ValidUsernameCharacters =
            "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890._";
        private const string ValidNicknameCharacters =
            "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890._- ";

        public static void ValidateUserRegistration(FindMyBuddiesContext context, UserModel model)
        {
            ValidateUsername(model.Username.Trim());
            ValidateNickname(model.Nickname.Trim());
            ValidateAuthCode(model.AuthCode.Trim());
            ValidateUserInDb(context, model);
        }

        public static User ValidateUserLogin(FindMyBuddiesContext context, UserModel model)
        {
            ValidateUsername(model.Username.Trim());
            ValidateAuthCode(model.AuthCode.Trim());
            var user = ValidateLoggedUserInDb(context, model);
            return user;
        }

        public static User ValidateSessionKey(FindMyBuddiesContext context, string sessionKey)
        {
            var user = context.Users.FirstOrDefault(u => u.SessionKey == sessionKey);

            if (user == null)
            {
                throw new ArgumentNullException("SessionKey dont't exists in the database");
            }

            return user;
        }

        public static void ValidateCoordinates(CoordinatesModel coordinates)
        {
            if (coordinates == null)
            {
                throw new ArgumentNullException("Coordinates can't be null");
            }
        }

        public static User ValidateFriendInDb(FindMyBuddiesContext context, int id, string nickname)
        {
            ValidateNickname(nickname.Trim());

            if (id < 0)
            {
                throw new ArgumentOutOfRangeException(string.Format(
                    "User Id can't have negative value : {0}", id));
            }

            var friendFound = context.Users.FirstOrDefault(u => u.Id == id);

            if (friendFound == null)
            {
                throw new ArgumentOutOfRangeException(string.Format(
                    "User Id don't exists in the database : {0}", id));
            }
            
            if (friendFound.Nickname.ToLower() != nickname.Trim().ToLower())
            {
                throw new ArgumentException(string.Format(
                    "User Id {0} and Nickname {1} missmatch.", id, nickname.Trim()));
            }

            return friendFound;
        }

        public static FriendRequest ValidateRequestExistence(
            FindMyBuddiesContext context, User userWhoRespondsToRequest, User friendWhoMadeRequest)
        {
            var request = userWhoRespondsToRequest.FriendRequests.FirstOrDefault(
                u => u.FromUserId == friendWhoMadeRequest.Id &&
                    u.FromUserNickname == friendWhoMadeRequest.Nickname);

            if (request == null)
            {
                throw new ArgumentException("Friend request don't exists");
            }

            return request;
        }

        public static void ValidateNickname(string nickname)
        {
            if (nickname == null)
            {
                throw new ArgumentNullException("Nickname can't be null");
            }
            else if (nickname.Length < MinUsernameAndNicknameLength)
            {
                throw new ArgumentOutOfRangeException(string.Format(
                    "Nickname should be at least {0} characters long", MinUsernameAndNicknameLength));
            }
            else if (nickname.Length > MaxUsernameAndNicknameLength)
            {
                throw new ArgumentOutOfRangeException(string.Format(
                    "Nickname should be less than {0} characters long", MaxUsernameAndNicknameLength));
            }
            else if (nickname.Any(ch => !ValidNicknameCharacters.Contains(ch)))
            {
                throw new ArgumentException(
                    "Nickname must contain only Latin letters, digits, ., _, -, [space]");
            }
        }

        private static void ValidateUsername(string username)
        {
            if (username == null)
            {
                throw new ArgumentNullException("Username can't be null");
            }
            else if (username.Length < MinUsernameAndNicknameLength)
            {
                throw new ArgumentOutOfRangeException(string.Format(
                    "Username should be at least {0} characters long", MinUsernameAndNicknameLength));
            }
            else if (username.Length > MaxUsernameAndNicknameLength)
            {
                throw new ArgumentOutOfRangeException(string.Format(
                    "Username should be less than {0} characters long", MaxUsernameAndNicknameLength));
            }
            else if (username.Any(ch => !ValidUsernameCharacters.Contains(ch)))
            {
                throw new ArgumentException(
                    "Username must contain only Latin letters, digits, ., _");
            }
        }

        private static void ValidateAuthCode(string authCode)
        {
            if (authCode == null)
            {
                throw new ArgumentNullException("AuthCode can't be null");
            }
            else if (authCode.Trim().Length != AuthcodeLength)
            {
                throw new ArgumentOutOfRangeException(string.Format(
                    "AuthCode should be {0} characters long", AuthcodeLength));
            }
        }

        private static void ValidateUserInDb(FindMyBuddiesContext context, UserModel model)
        {
            string usernameToLower = model.Username.Trim().ToLower();
            var userByUsername = context.Users.FirstOrDefault(u => u.Username == usernameToLower);
            if (userByUsername != null)
            {
                throw new InvalidOperationException("Username exists");
            }

            string nicknameToLower = model.Nickname.Trim().ToLower();
            var userByNickname = context.Users.FirstOrDefault(u => u.Nickname.ToLower() == nicknameToLower);
            if (userByNickname != null)
            {
                throw new InvalidOperationException("Nickname exists");
            }
        }

        private static User ValidateLoggedUserInDb(FindMyBuddiesContext context, UserModel model)
        {
            string usernameToLowerAndTrimmed = model.Username.Trim().ToLower();
            string authCodeTrimmed = model.AuthCode.Trim();

            var user = context.Users.FirstOrDefault(
                    u => u.Username == usernameToLowerAndTrimmed &&
                    u.AuthCode == authCodeTrimmed);

            if (user == null)
            {
                throw new InvalidOperationException("Invalid username or password");
            }

            return user;
        }
    }
}