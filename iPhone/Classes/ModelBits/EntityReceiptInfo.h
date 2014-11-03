//
//  EntityReceiptInfo.h
//  ConcurMobile
//
//  Created by charlottef on 3/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityReceiptInfo : NSManagedObject

@property (nonatomic, retain) NSString * receiptId;
@property (nonatomic, retain) NSString * imageUrl;
@property (nonatomic, retain) NSString * thumbUrl;
@property (nonatomic, retain) NSDate * dateCreated;
@property (nonatomic, retain) NSString * imageType;

@end
