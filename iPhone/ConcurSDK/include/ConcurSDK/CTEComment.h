//
//  CTEComment.h
//  ConcurSDK
//
//  Created by laurent mery on 02/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  ** build phases : to be include in <copy file> **
//


#import <Foundation/Foundation.h>
#import "CTEDataTypes.h"

@interface CTEComment : NSObject

@property (copy, nonatomic) CTEDataTypes *Comment;
@property (copy, nonatomic) CTEDataTypes *FirstName;
@property (copy, nonatomic) CTEDataTypes *LastName;
@property (copy, nonatomic) CTEDataTypes *DateTime;
@property (copy, nonatomic) CTEDataTypes *IsLatest;

//add by model
@property (copy, nonatomic) CTEDataTypes *CommentLight;


/*
 * remove datetime part on comment
 */
- (id)valueForUndefinedKey:(NSString *)key;

@end