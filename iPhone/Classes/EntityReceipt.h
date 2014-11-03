//
//  EntityReceipt.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 6/2/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityReceipt : NSManagedObject {
@private
}
@property (nonatomic, strong) NSDate * dateLastModified;
@property (nonatomic, strong) NSDate * dateCreated;
@property (nonatomic, strong) NSString * imageID;
@property (nonatomic, strong) NSString * thumbUrl;
@property (nonatomic, strong) NSString * tag;
@property (nonatomic, strong) NSString * key;
@property (nonatomic, strong) NSString * type;
@property (nonatomic, strong) NSString * fullscreenImageLocalPath;
@property (nonatomic, strong) NSString * fullscreenUrl;
@property (nonatomic, strong) NSManagedObject * Image;

@end
