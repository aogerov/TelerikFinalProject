using System;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using FindMyBuddies.Api.Assists;
using FindMyBuddies.Api.Models;
using FindMyBuddies.Data;

namespace FindMyBuddies.Api.Controllers
{
    public class CoordinatesController : BaseApiController
    {
        // api/coordinates/update?sessionKey={sessionKey}
        [HttpPost]
        [ActionName("update")]
        public HttpResponseMessage UpdateCoordinates([FromBody]CoordinatesModel model, [FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    Validator.ValidateCoordinates(model);

                    var user = Validator.ValidateSessionKey(context, sessionKey);
                    if (user.Coordinates != null)
                    {
                        user.Coordinates.Latitude = model.Latitude;
                        user.Coordinates.Longitude = model.Longitude;
                        user.Coordinates.Timestamp = DateTime.Now;
                    }
                    else
                    {
                        var newCoordinates = Parser.CoordinatesModelToCoordinates(model);
                        newCoordinates.Timestamp = DateTime.Now;
                        context.Coordinates.Add(newCoordinates);
                        user.Coordinates = newCoordinates;
                    }

                    context.SaveChanges();

                    var response = this.Request.CreateResponse(HttpStatusCode.OK);
                    return response;
                }
            });

            return responseMsg;
        }
    }
}
