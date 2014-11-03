//
//  Detail.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface Detail : NSObject 
{
	NSString			*lbl, *val, *code, *codeName, *mapAddress, *url, *accessoryImageName, *actionType, *detailTitle;
	UIImage				*imgDetail;
}

@property (nonatomic, strong) NSString			*detailTitle;
@property (nonatomic, strong) NSString			*lbl;
@property (nonatomic, strong) NSString			*val;
@property (nonatomic, strong) NSString			*code;
@property (nonatomic, strong) NSString			*codeName;
@property (nonatomic, strong) NSString			*mapAddress;
@property (nonatomic, strong) NSString			*url;
@property (nonatomic, strong) NSString			*accessoryImageName;
@property (nonatomic, strong) NSString			*actionType;
@property (nonatomic, strong) UIImage			*imgDetail;
@end
