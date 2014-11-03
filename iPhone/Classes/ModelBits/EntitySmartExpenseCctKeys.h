//
//  EntitySmartExpenseCctKeys.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntitySmartExpenseCctKeys : NSManagedObject {
@private
}
@property (nonatomic, strong) NSString * meKey;
@property (nonatomic, strong) NSString * cctKey;

@end
