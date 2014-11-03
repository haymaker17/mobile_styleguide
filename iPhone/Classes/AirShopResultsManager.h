//
//  AirShopResultsManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AirShopResultsManager : NSObject {
    NSManagedObjectContext      *_context;
    NSString *entityName;
}

@property (nonatomic, strong) NSManagedObjectContext *context;
@property (nonatomic, strong) NSString *entityName;

-(void) saveIt:(NSManagedObject *) obj;
-(BOOL) hasAny;
-(NSManagedObject *) makeNew;
-(NSManagedObject *) fetchFirst;
-(NSArray *) fetchAll;
-(NSManagedObject *) fetchOrMake:(NSString *)key;
//- (NSFetchedResultsController *)fetchedResultsController: (MobileViewController *) mvc entityName:(NSString *) entityName fetchedRC:(NSFetchedResultsController *) fetchedRC sectionNameKeyPath:(NSString *) sectionNameKeyPath;
-(void) deleteObj:(NSManagedObject *)obj;
-(void) deleteAll;

+(AirShopResultsManager*)sharedInstance;
-(AirShopResultsManager*)init;


@end
