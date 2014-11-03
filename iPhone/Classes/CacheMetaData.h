//
//  CacheMetaData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/26/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CacheMetaData : NSObject 
{
	NSString	*fileName, *msgKey, *msgType, *recordKey, *userID, *cacheKey;
	NSDate		*dateModified, *dateCreated, *dateExpires;
	BOOL		needsRefresh;
}

@property (strong, nonatomic) NSString		*fileName;		//the actual name of the file on disk
@property (strong, nonatomic) NSString		*msgKey;		//The idKey from the Msg object
@property (strong, nonatomic) NSString		*msgType;		//Also the idKey from the Msg, might be used later for a section break down
@property (strong, nonatomic) NSString		*recordKey;		//the record key associated with a file
@property (strong, nonatomic) NSString		*userID;		//the userid, retrieved from the server
@property (strong, nonatomic) NSString		*cacheKey;		//the combined value that makes this cache file name unique
@property (strong, nonatomic) NSDate		*dateModified;
@property (strong, nonatomic) NSDate		*dateCreated;
@property (strong, nonatomic) NSDate		*dateExpires;	//from the server, a date time as to when this cache file should just be blown away.
@property (nonatomic) BOOL					needsRefresh;	//refresh from server is needed, but cache is retained since server may not be available

@end
