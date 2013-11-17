using System;
using System.Runtime.Serialization;

namespace FindMyBuddies.Api.Models
{
    [DataContract]
    public class ImageModel
    {
        [DataMember(Name = "url")]
        public string Url { get; set; }

        [DataMember(Name = "thumbUrl")]
        public string ThumbUrl { get; set; }

        [DataMember(Name = "imageDateAsString")]
        public String ImageDateAsString { get; set; }

        [DataMember(Name = "timestampDifferenceWithCoordinates")]
        public string TimestampDifferenceWithCoordinates { get; set; }

        [DataMember(Name = "coordinatesAccuracy")]
        public string CoordinatesAccuracy { get; set; }

        [DataMember(Name = "latitude")]
        public double Latitude { get; set; }

        [DataMember(Name = "longitude")]
        public double Longitude { get; set; }
    }
}