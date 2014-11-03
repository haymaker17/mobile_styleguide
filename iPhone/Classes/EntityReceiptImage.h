//
//  EntityReceiptImage.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 6/7/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityReceipt;

@interface EntityReceiptImage : NSManagedObject {
@private
}
@property (nonatomic, strong) NSData * image;
@property (nonatomic, strong) NSString * key;
@property (nonatomic, strong) EntityReceipt * Receipt;

@end
