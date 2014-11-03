//
//  CommentData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CommentData.h"


@implementation CommentData
@synthesize comment, commentBy, commentKey, creationDate, isLatest, rpeKey, rptKey;



#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:comment	forKey:@"comment"];
	[coder encodeObject:commentBy	forKey:@"commentBy"];
    [coder encodeObject:commentKey	forKey:@"commentKey"];
	[coder encodeObject:creationDate	forKey:@"creationDate"];
    [coder encodeObject:isLatest	forKey:@"isLatest"];
	[coder encodeObject:rpeKey	forKey:@"rpeKey"];
    [coder encodeObject:rptKey	forKey:@"rptKey"];
}

- (id)initWithCoder:(NSCoder *)coder {
    self.comment = [coder decodeObjectForKey:@"comment"];
	self.commentBy = [coder decodeObjectForKey:@"commentBy"];
    self.commentKey = [coder decodeObjectForKey:@"commentKey"];
	self.creationDate = [coder decodeObjectForKey:@"creationDate"];
    self.isLatest = [coder decodeObjectForKey:@"isLatest"];
	self.rpeKey = [coder decodeObjectForKey:@"rpeKey"];
    self.rptKey = [coder decodeObjectForKey:@"rptKey"];
    return self;
}


@end
