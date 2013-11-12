using System;
using System.Linq;
using System.Text;

namespace FindMyBuddies.Api.Assists
{
    public class Generator
    {
        private const string SessionKeyChars = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
        private const int SessionKeyLength = 50;
        private static readonly Random rnd = new Random();

        public static string GenerateSessionKey(int id)
        {
            var sessionKey = new StringBuilder();
            sessionKey.Append(id);
            while (sessionKey.Length < SessionKeyLength)
            {
                int index = rnd.Next(SessionKeyChars.Length);
                char ch = SessionKeyChars[index];
                sessionKey.Append(ch);
            }

            return sessionKey.ToString();
        }

        //public static Post CreateNewPost(
        //    BloggingSystemContext context, PostModelCreate model, string sessionKey)
        //{
        //    var newPost = new Post
        //    {
        //        Title = model.Title,
        //        Text = model.Text,
        //        PostDate = DateTime.Now,
        //    };

        //    var user = context.Users.First(u => u.SessionKey == sessionKey);
        //    newPost.User = user;

        //    if (model.Tags != null)
        //    {
        //        foreach (var tagName in model.Tags)
        //        {
        //            var tagNameToLower = tagName.ToLower();
        //            var tagInDb = context.Tags.FirstOrDefault(t => t.Name == tagNameToLower);

        //            if (tagInDb != null)
        //            {
        //                newPost.Tags.Add(tagInDb);
        //            }
        //            else
        //            {
        //                var newTag = new Tag
        //                {
        //                    Name = tagNameToLower
        //                };

        //                newPost.Tags.Add(newTag);
        //            }
        //        }
        //    }

        //    return newPost;
        //}

        //public static Comment CreateNewComment(
        //    BloggingSystemContext context, int postId, CommentModel model, string sessionKey)
        //{
        //    var newComment = new Comment
        //    {
        //        Text = model.Text,
        //        PostDate = DateTime.Now,
        //        User = context.Users.First(u => u.SessionKey == sessionKey),
        //        Post = context.Posts.First(p => p.Id == postId)
        //    };

        //    return newComment;
        //}
    }
}