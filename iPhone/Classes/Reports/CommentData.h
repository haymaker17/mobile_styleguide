//
//  CommentData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CommentData : NSObject 
{
	NSString			*comment, *commentBy, *commentKey, *creationDate, *isLatest, *rpeKey, *rptKey;

}


@property (strong, nonatomic) NSString *comment;
@property (strong, nonatomic) NSString *commentBy;
@property (strong, nonatomic) NSString *commentKey;
@property (strong, nonatomic) NSString *creationDate;
@property (strong, nonatomic) NSString *isLatest;
@property (strong, nonatomic) NSString *rpeKey;
@property (strong, nonatomic) NSString *rptKey;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

@end
