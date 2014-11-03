//
//  CTETravelRequestComment.h
//  ConcurSDK
//
//  Created by laurent mery on 02/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  ** build phases : to be include in <copy file> **
//


#import <Foundation/Foundation.h>

@interface CTETravelRequestComment : NSObject

@property (copy, nonatomic) NSString *Comment;
@property (copy, nonatomic) NSString *FirstName;
@property (copy, nonatomic) NSString *LastName;
@property (copy, nonatomic) NSString *DateTime;
@property (copy, nonatomic) NSString *IsLatest;

/*
 * remove datetime part on comment
 */
-(NSString*)getCommentTextOnly;
- (id)valueForUndefinedKey:(NSString *)key;

@end