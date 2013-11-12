using System;
using System.Runtime.Serialization;

namespace FindMyBuddies.Api.Models
{
    [DataContract]
    public class ImageModel
    {
        [DataMember(Name = "url")]
        public string Url { get; set; }

        [DataMember(Name = "timestamp")]
        public DateTime Timestamp { get; set; }

        [DataMember(Name = "latitudeAtCapturing")]
        public string LatitudeAtCapturing { get; set; }

        [DataMember(Name = "longitudeAtCapturing")]
        public string LongitudeAtCapturing { get; set; }
    }
}